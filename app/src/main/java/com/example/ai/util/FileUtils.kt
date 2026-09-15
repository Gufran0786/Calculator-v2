package com.example.ai.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.media.ExifInterface
import com.example.ai.model.AttachedFile
import com.example.ai.model.AttachmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object FileUtils {

    suspend fun processUri(context: Context, uri: Uri): AttachedFile? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromUri(uri)
            val fileName = getFileName(context, uri) ?: "attachment_${System.currentTimeMillis()}"

            val attachmentType = when {
                mimeType.startsWith("image/") -> AttachmentType.IMAGE
                mimeType == "application/pdf" -> AttachmentType.PDF_DOCUMENT
                mimeType.startsWith("text/") || fileName.endsWith(".txt") -> AttachmentType.TEXT_DOCUMENT
                else -> AttachmentType.GENERIC_FILE
            }

            if (attachmentType == AttachmentType.IMAGE) {
                // Compress & downscale image efficiently to prevent oversized payloads
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val rotatedBitmap = rotateBitmapIfRequired(context, uri, originalBitmap)
                    val scaledBitmap = scaleBitmapDown(rotatedBitmap, 1024)
                    val outputStream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 78, outputStream)
                    val byteArray = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

                    return@withContext AttachedFile(
                        name = fileName,
                        mimeType = "image/jpeg",
                        base64Data = base64String,
                        sizeBytes = byteArray.size.toLong(),
                        localUriString = uri.toString(),
                        attachmentType = AttachmentType.IMAGE
                    )
                }
            }

            // For other documents / PDFs
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null && bytes.isNotEmpty()) {
                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                return@withContext AttachedFile(
                    name = fileName,
                    mimeType = mimeType,
                    base64Data = base64String,
                    sizeBytes = bytes.size.toLong(),
                    localUriString = uri.toString(),
                    attachmentType = attachmentType
                )
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun processBitmap(bitmap: Bitmap, namePrefix: String = "camera_capture"): AttachedFile = withContext(Dispatchers.IO) {
        val scaledBitmap = scaleBitmapDown(bitmap, 1024)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 78, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)

        AttachedFile(
            name = "${namePrefix}_${System.currentTimeMillis()}.jpg",
            mimeType = "image/jpeg",
            base64Data = base64String,
            sizeBytes = byteArray.size.toLong(),
            localUriString = null,
            attachmentType = AttachmentType.IMAGE
        )
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int

        if (width > height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt()
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    private fun rotateBitmapIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return bitmap
            val ei = ExifInterface(input)
            val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            input.close()

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        if (name == null) {
            name = uri.lastPathSegment
        }
        return name
    }

    private fun getMimeTypeFromUri(uri: Uri): String {
        val path = uri.path ?: return "application/octet-stream"
        return when {
            path.endsWith(".jpg", true) || path.endsWith(".jpeg", true) -> "image/jpeg"
            path.endsWith(".png", true) -> "image/png"
            path.endsWith(".webp", true) -> "image/webp"
            path.endsWith(".pdf", true) -> "application/pdf"
            path.endsWith(".txt", true) -> "text/plain"
            else -> "application/octet-stream"
        }
    }
}
