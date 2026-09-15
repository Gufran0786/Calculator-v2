package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.CalculationHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CalculatorRepository(private val context: Context) {
    private val historyDao = AppDatabase.getDatabase(context).historyDao()
    private val prefs = context.getSharedPreferences("calculator_settings", Context.MODE_PRIVATE)

    fun getAllHistory(): Flow<List<CalculationHistory>> = historyDao.getAllHistory()

    suspend fun insertHistory(expression: String, result: String) {
        withContext(Dispatchers.IO) {
            historyDao.insert(
                CalculationHistory(
                    expression = expression,
                    result = result,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteHistory(history: CalculationHistory) {
        withContext(Dispatchers.IO) {
            historyDao.delete(history)
        }
    }

    suspend fun clearAllHistory() {
        withContext(Dispatchers.IO) {
            historyDao.clearAll()
        }
    }

    fun getBackgroundPath(): String? {
        val path = prefs.getString(KEY_BG_PATH, null)
        if (path != null && File(path).exists()) {
            return path
        }
        return null
    }

    suspend fun saveCustomBackground(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val destinationFile = File(context.filesDir, "custom_wallpaper.jpg")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                val path = destinationFile.absolutePath
                prefs.edit().putString(KEY_BG_PATH, path).apply()
                path
            } catch (e: Exception) {
                null
            }
        }
    }

    fun resetBackgroundToDefault() {
        prefs.edit().remove(KEY_BG_PATH).apply()
        val file = File(context.filesDir, "custom_wallpaper.jpg")
        if (file.exists()) {
            file.delete()
        }
    }

    fun getBlurRadius(): Float {
        return prefs.getFloat(KEY_BLUR_RADIUS, 22f)
    }

    fun setBlurRadius(radius: Float) {
        prefs.edit().putFloat(KEY_BLUR_RADIUS, radius).apply()
    }

    fun getOverlayDarkness(): Float {
        return prefs.getFloat(KEY_OVERLAY_DARKNESS, 0.38f)
    }

    fun setOverlayDarkness(darkness: Float) {
        prefs.edit().putFloat(KEY_OVERLAY_DARKNESS, darkness).apply()
    }

    fun getAppTheme(): String {
        return prefs.getString(KEY_APP_THEME, "frosted_dark") ?: "frosted_dark"
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString(KEY_APP_THEME, theme).apply()
    }

    fun isHapticEnabled(): Boolean {
        return prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
    }

    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    fun isScientificMode(): Boolean {
        return prefs.getBoolean(KEY_SCIENTIFIC_MODE, false)
    }

    fun setScientificMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SCIENTIFIC_MODE, enabled).apply()
    }

    fun isDegreeMode(): Boolean {
        return prefs.getBoolean(KEY_DEGREE_MODE, true)
    }

    fun setDegreeMode(isDegree: Boolean) {
        prefs.edit().putBoolean(KEY_DEGREE_MODE, isDegree).apply()
    }

    companion object {
        private const val KEY_BG_PATH = "custom_bg_path"
        private const val KEY_BLUR_RADIUS = "blur_radius"
        private const val KEY_OVERLAY_DARKNESS = "overlay_darkness"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_SCIENTIFIC_MODE = "scientific_mode"
        private const val KEY_DEGREE_MODE = "degree_mode"
    }
}
