package com.example.ai.ui

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ai.camera.CameraScannerModal
import com.example.ai.model.AttachedFile
import com.example.ai.model.AttachmentType
import com.example.ai.model.MathSolution
import com.example.ai.model.SolutionStep
import com.example.ai.service.GeminiMathService
import com.example.ai.service.LocalMathEngine
import com.example.ai.util.FileUtils
import com.example.data.db.AiMathHistory
import com.example.data.db.AppDatabase
import com.example.ui.components.GlassSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMathSolverScreen(
    onInsertExpressionToCalc: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val geminiService = remember { GeminiMathService(context) }
    val db = remember { AppDatabase.getDatabase(context) }

    var questionInput by remember { mutableStateOf("") }
    var attachedFiles by remember { mutableStateOf<List<AttachedFile>>(emptyList()) }
    var isSolving by remember { mutableStateOf(false) }
    var solveJob by remember { mutableStateOf<Job?>(null) }
    var currentSolution by remember { mutableStateOf<MathSolution?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dialog & Sheets
    var isCameraOpen by remember { mutableStateOf(false) }
    var isApiKeyDialogOpen by remember { mutableStateOf(false) }
    var isHistorySheetOpen by remember { mutableStateOf(false) }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Follow up state
    var followUpInput by remember { mutableStateOf("") }
    var isFollowUpLoading by remember { mutableStateOf(false) }
    var followUpResponse by remember { mutableStateOf<String?>(null) }

    // History list from Room DB
    val historyList by db.aiMathDao().getAllAiHistory().collectAsState(initial = emptyList())

    // Activity Result Launchers for Media/Documents
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val processed = FileUtils.processUri(context, uri)
                if (processed != null) {
                    attachedFiles = attachedFiles + processed
                } else {
                    Toast.makeText(context, "Could not process selected image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val processed = FileUtils.processUri(context, uri)
                if (processed != null) {
                    attachedFiles = attachedFiles + processed
                } else {
                    Toast.makeText(context, "Could not process selected file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val executeInstantLocalSolve = {
        if (questionInput.isNotBlank()) {
            val localSol = LocalMathEngine.solveLocally(questionInput)
            if (localSol != null) {
                currentSolution = localSol
                errorMessage = null
                scope.launch {
                    saveSolutionToDb(db, localSol, false)
                }
                Toast.makeText(context, "Solved Instantly!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Complex question - using Gemini AI...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val executeSolve = {
        if (questionInput.isBlank() && attachedFiles.isEmpty()) {
            Toast.makeText(context, "Please enter a question or attach a photo/document", Toast.LENGTH_SHORT).show()
        } else {
            // Cancel previous job if running
            solveJob?.cancel()

            isSolving = true
            errorMessage = null
            followUpResponse = null

            solveJob = scope.launch {
                val result = geminiService.solveMathProblem(questionInput, attachedFiles)
                isSolving = false
                result.fold(
                    onSuccess = { solution ->
                        currentSolution = solution
                        // Save to Room DB
                        saveSolutionToDb(db, solution, attachedFiles.isNotEmpty())
                    },
                    onFailure = { ex ->
                        errorMessage = ex.message ?: "Failed to solve math problem."
                    }
                )
            }
        }
    }

    val sampleProblems = listOf(
        "x² - 5x + 6 = 0",
        "d/dx (x³ · sin(x))",
        "∫ (3x² + 4x - 1) dx",
        "Find area of triangle with sides 5, 12, 13",
        "Solve: 2x + 3y = 12 and x - y = 1",
        "A train travels 120 km in 2 hrs. Calculate speed in m/s"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 600.dp)
            .testTag("ai_math_solver_screen")
    ) {
        // AI Header & Status Bar
        GlassSurface(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.8f),
            borderColor = Color(0xFF38BDF8).copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF8B5CF6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gemini Math AI",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Step-by-Step",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Solve with Camera, Photos, Docs & Text",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // API Key Configuration Button
                    val hasKey = geminiService.getApiKey().isNotEmpty()
                    IconButton(
                        onClick = { isApiKeyDialogOpen = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("btn_ai_api_key")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "API Key",
                            tint = if (hasKey) Color(0xFF10B981) else Color(0xFFFBBF24),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // History Button
                    BadgedBox(
                        badge = {
                            if (historyList.isNotEmpty()) {
                                Badge(containerColor = Color(0xFF0284C7)) {
                                    Text(historyList.size.toString())
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { isHistorySheetOpen = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                .testTag("btn_ai_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "AI History",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Question Input Box
        GlassSurface(
            shape = RoundedCornerShape(20.dp),
            backgroundColor = Color(0xFF1E293B).copy(alpha = 0.7f),
            borderColor = Color.White.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enter or Describe Problem",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (questionInput.isNotEmpty() || attachedFiles.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    questionInput = ""
                                    attachedFiles = emptyList()
                                }
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = questionInput,
                    onValueChange = { questionInput = it },
                    placeholder = {
                        Text(
                            text = "Type math question, quadratic formula, derivative, integral, calculus limit, geometry, or word problem...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFF38BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("input_ai_math_question")
                )

                // Attached Files Strip
                if (attachedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Attached Files (${attachedFiles.size})",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        attachedFiles.forEachIndexed { index, file ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = when (file.attachmentType) {
                                            AttachmentType.IMAGE -> Icons.Default.Image
                                            AttachmentType.PDF_DOCUMENT -> Icons.Default.Description
                                            else -> Icons.Default.AttachFile
                                        },
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Column {
                                        Text(
                                            text = file.name.take(18) + if (file.name.length > 18) "..." else "",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${file.sizeBytes / 1024} KB",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            attachedFiles = attachedFiles.toMutableList().also { it.removeAt(index) }
                                        },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove attachment",
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Dock: Camera, Gallery Photos, Document Files
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Scanner Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { isCameraOpen = true }
                            .padding(vertical = 10.dp)
                            .testTag("btn_ai_camera_scan"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera Scanner",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Camera",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Gallery Photos Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_ai_pick_photo"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Photos",
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Photos",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Documents / Files Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable {
                                documentPickerLauncher.launch("*/*")
                            }
                            .padding(vertical = 10.dp)
                            .testTag("btn_ai_pick_document"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Document / PDF",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Document",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Sample Quick Questions Horizontal Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sampleProblems.forEach { problem ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .clickable { questionInput = problem }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = problem,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row: Solve AI & Instant Solve
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main AI Solve Button
            Button(
                onClick = { executeSolve() },
                enabled = !isSolving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1), Color(0xFF9333EA))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .testTag("btn_solve_math_problem")
            ) {
                if (isSolving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Solving...",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Solve with AI",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cancel Button while solving
            if (isSolving) {
                Button(
                    onClick = {
                        solveJob?.cancel()
                        isSolving = false
                        Toast.makeText(context, "Solving cancelled", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Cancel", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else if (questionInput.isNotBlank()) {
                // Instant Local Solve Button
                Button(
                    onClick = { executeInstantLocalSolve() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("⚡ Fast (<0.1s)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error Banner with Offline Local Solve Fallback if any
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            GlassSurface(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color(0xFF7F1D1D).copy(alpha = 0.6f),
                borderColor = Color(0xFFEF4444),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = Color(0xFFFCA5A5)
                        )
                        Text(
                            text = "Error Solving Question",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { isApiKeyDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Configure API Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        if (questionInput.isNotBlank()) {
                            Button(
                                onClick = {
                                    val sol = LocalMathEngine.solveLocally(questionInput)
                                    if (sol != null) {
                                        currentSolution = sol
                                        errorMessage = null
                                    } else {
                                        Toast.makeText(context, "Cannot solve locally. Please verify API key.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Try Offline Solver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Solution Display
        if (currentSolution != null) {
            val sol = currentSolution!!
            Spacer(modifier = Modifier.height(18.dp))

            // Topic Pill & Problem Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0284C7).copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Topic: ${sol.topic}",
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified AI Solution",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Final Answer Highlight Box
            GlassSurface(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = Color(0xFF064E3B).copy(alpha = 0.7f),
                borderColor = Color(0xFF10B981),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FINAL ANSWER",
                            color = Color(0xFF6EE7B7),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Copy button
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sol.finalAnswer))
                                    Toast.makeText(context, "Final answer copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Answer",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Insert to Calculator button
                            Button(
                                onClick = {
                                    // Extract numeric or clean equation value
                                    val cleanAns = sol.finalAnswer.substringAfter("=").trim()
                                    onInsertExpressionToCalc(cleanAns.ifEmpty { sol.finalAnswer })
                                    Toast.makeText(context, "Sent to Calculator!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Use in Calc", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = sol.finalAnswer,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Strategy Summary
            if (sol.summary.isNotBlank()) {
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF1E293B).copy(alpha = 0.6f),
                    borderColor = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Approach & Strategy",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sol.summary,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Step-by-Step Breakdown Cards
            Text(
                text = "Step-by-Step Solution (${sol.steps.size} Steps)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            sol.steps.forEach { step ->
                StepCard(step = step)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Key Formulas Used Card
            if (sol.keyFormulas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF1E1B4B).copy(alpha = 0.6f),
                    borderColor = Color(0xFF818CF8).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = Color(0xFFA5B4FC),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Formulas & Rules Used",
                                color = Color(0xFFA5B4FC),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        sol.keyFormulas.forEach { formula ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.3f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = formula,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Tips & Verification Section
            if (sol.tips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color(0xFF451A03).copy(alpha = 0.5f),
                    borderColor = Color(0xFFF59E0B).copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFCD34D),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Tips & Verification",
                                color = Color(0xFFFCD34D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        sol.tips.forEach { tip ->
                            Text(
                                text = "• $tip",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Follow-up Question Section
            Spacer(modifier = Modifier.height(16.dp))
            GlassSurface(
                shape = RoundedCornerShape(18.dp),
                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.85f),
                borderColor = Color(0xFF38BDF8).copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Have a Doubt? Ask AI to Clarify",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = followUpInput,
                            onValueChange = { followUpInput = it },
                            placeholder = {
                                Text(
                                    text = "Ask why a step was taken or request another method...",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 12.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                cursorColor = Color(0xFF38BDF8)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("input_ai_follow_up")
                        )

                        IconButton(
                            onClick = {
                                if (followUpInput.isNotBlank() && !isFollowUpLoading) {
                                    isFollowUpLoading = true
                                    val q = followUpInput
                                    followUpInput = ""
                                    scope.launch {
                                        val res = geminiService.askFollowUp(sol, q)
                                        isFollowUpLoading = false
                                        res.fold(
                                            onSuccess = { followUpResponse = it },
                                            onFailure = { followUpResponse = "Could not get clarification: ${it.message}" }
                                        )
                                    }
                                }
                            },
                            enabled = followUpInput.isNotBlank() && !isFollowUpLoading,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0284C7), CircleShape)
                                .testTag("btn_send_follow_up")
                        ) {
                            if (isFollowUpLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (followUpResponse != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = followUpResponse!!,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Camera Scanner View
    CameraScannerModal(
        isOpen = isCameraOpen,
        onClose = { isCameraOpen = false },
        onPhotoCaptured = { bitmap ->
            scope.launch {
                val processed = FileUtils.processBitmap(bitmap)
                attachedFiles = attachedFiles + processed
                Toast.makeText(context, "Photo attached to AI Solver", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // API Key Dialog
    ApiKeyDialog(
        currentKey = geminiService.getApiKey(),
        isOpen = isApiKeyDialogOpen,
        onDismiss = { isApiKeyDialogOpen = false },
        onSaveKey = { key ->
            geminiService.saveCustomApiKey(key)
            Toast.makeText(context, "API Key Saved", Toast.LENGTH_SHORT).show()
        }
    )

    // History Sheet
    AiMathHistorySheet(
        isOpen = isHistorySheetOpen,
        sheetState = historySheetState,
        historyList = historyList,
        onDismiss = { isHistorySheetOpen = false },
        onSelectHistory = { item ->
            // Re-inflate solution
            currentSolution = parseSolutionFromJson(item.solutionJson, item.problemText, item.topic, item.finalAnswer)
            questionInput = item.problemText
        },
        onDeleteHistory = { id ->
            scope.launch { db.aiMathDao().deleteById(id) }
        },
        onClearAll = {
            scope.launch { db.aiMathDao().clearAll() }
        }
    )
}

@Composable
fun StepCard(step: SolutionStep) {
    GlassSurface(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0xFF1E293B).copy(alpha = 0.75f),
        borderColor = Color.White.copy(alpha = 0.15f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(Color(0xFF0284C7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${step.stepNumber}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = step.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (step.mathExpression != null && step.mathExpression.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = step.mathExpression,
                        color = Color(0xFF38BDF8),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (step.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.explanation,
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            if (step.subSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                step.subSteps.forEach { sub ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        Text(
                            text = sub,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private suspend fun saveSolutionToDb(db: AppDatabase, solution: MathSolution, hasAttachment: Boolean) = withContext(Dispatchers.IO) {
    try {
        val root = JSONObject().apply {
            put("problemText", solution.problemText)
            put("topic", solution.topic)
            put("summary", solution.summary)
            put("finalAnswer", solution.finalAnswer)
            put("confidence", solution.confidence)

            val stepsArr = JSONArray()
            solution.steps.forEach { step ->
                val sObj = JSONObject().apply {
                    put("stepNumber", step.stepNumber)
                    put("title", step.title)
                    put("explanation", step.explanation)
                    put("mathExpression", step.mathExpression ?: "")
                    val subArr = JSONArray()
                    step.subSteps.forEach { subArr.put(it) }
                    put("subSteps", subArr)
                }
                stepsArr.put(sObj)
            }
            put("steps", stepsArr)

            val formArr = JSONArray()
            solution.keyFormulas.forEach { formArr.put(it) }
            put("keyFormulas", formArr)

            val tipsArr = JSONArray()
            solution.tips.forEach { tipsArr.put(it) }
            put("tips", tipsArr)
        }

        val item = AiMathHistory(
            problemText = solution.problemText,
            topic = solution.topic,
            finalAnswer = solution.finalAnswer,
            solutionJson = root.toString(),
            hasAttachment = hasAttachment,
            timestamp = solution.timestamp
        )
        db.aiMathDao().insertAiHistory(item)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun parseSolutionFromJson(jsonString: String, fallbackProblem: String, fallbackTopic: String, fallbackAnswer: String): MathSolution {
    return try {
        val json = JSONObject(jsonString)
        val steps = mutableListOf<SolutionStep>()
        val stepsArr = json.optJSONArray("steps")
        if (stepsArr != null) {
            for (i in 0 until stepsArr.length()) {
                val sObj = stepsArr.optJSONObject(i) ?: continue
                val subs = mutableListOf<String>()
                val subArr = sObj.optJSONArray("subSteps")
                if (subArr != null) {
                    for (j in 0 until subArr.length()) subs.add(subArr.optString(j))
                }
                steps.add(
                    SolutionStep(
                        stepNumber = sObj.optInt("stepNumber", i + 1),
                        title = sObj.optString("title", "Step ${i + 1}"),
                        explanation = sObj.optString("explanation", ""),
                        mathExpression = sObj.optString("mathExpression").takeIf { it.isNotBlank() },
                        subSteps = subs
                    )
                )
            }
        }

        val formulas = mutableListOf<String>()
        val formArr = json.optJSONArray("keyFormulas")
        if (formArr != null) {
            for (i in 0 until formArr.length()) formulas.add(formArr.optString(i))
        }

        val tips = mutableListOf<String>()
        val tipsArr = json.optJSONArray("tips")
        if (tipsArr != null) {
            for (i in 0 until tipsArr.length()) tips.add(tipsArr.optString(i))
        }

        MathSolution(
            problemText = json.optString("problemText", fallbackProblem),
            topic = json.optString("topic", fallbackTopic),
            summary = json.optString("summary", ""),
            steps = steps,
            finalAnswer = json.optString("finalAnswer", fallbackAnswer),
            keyFormulas = formulas,
            tips = tips
        )
    } catch (e: Exception) {
        MathSolution(
            problemText = fallbackProblem,
            topic = fallbackTopic,
            summary = "",
            steps = listOf(SolutionStep(1, "Solution", fallbackAnswer)),
            finalAnswer = fallbackAnswer
        )
    }
}
