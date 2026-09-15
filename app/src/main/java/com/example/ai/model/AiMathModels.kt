package com.example.ai.model

data class SolutionStep(
    val stepNumber: Int,
    val title: String,
    val explanation: String,
    val mathExpression: String? = null,
    val subSteps: List<String> = emptyList()
)

data class MathSolution(
    val problemText: String,
    val topic: String,
    val summary: String,
    val steps: List<SolutionStep>,
    val finalAnswer: String,
    val keyFormulas: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val confidence: String = "High",
    val rawResponse: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class AttachmentType {
    IMAGE,
    PDF_DOCUMENT,
    TEXT_DOCUMENT,
    GENERIC_FILE
}

data class AttachedFile(
    val name: String,
    val mimeType: String,
    val base64Data: String,
    val sizeBytes: Long,
    val localUriString: String? = null,
    val attachmentType: AttachmentType = AttachmentType.IMAGE
)

sealed class AiSolverUiState {
    object Idle : AiSolverUiState()
    data class Loading(val message: String = "AI is analyzing the math problem...") : AiSolverUiState()
    data class Success(val solution: MathSolution) : AiSolverUiState()
    data class Error(val errorMessage: String, val canRetry: Boolean = true) : AiSolverUiState()
}
