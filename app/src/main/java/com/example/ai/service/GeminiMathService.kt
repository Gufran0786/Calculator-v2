package com.example.ai.service

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.ai.model.AttachedFile
import com.example.ai.model.MathSolution
import com.example.ai.model.SolutionStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiMathService(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val PREFS_NAME = "gemini_prefs"
    private val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"

    fun getApiKey(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(KEY_CUSTOM_API_KEY, "")?.trim() ?: ""
        if (customKey.isNotEmpty()) return customKey

        return try {
            val buildConfigKey = BuildConfig.GEMINI_API_KEY
            if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_GEMINI_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun saveCustomApiKey(key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    suspend fun solveMathProblem(
        questionText: String,
        attachments: List<AttachedFile> = emptyList()
    ): Result<MathSolution> = withContext(Dispatchers.IO) {
        val trimmedQuestion = questionText.trim()
        val apiKey = getApiKey()

        // If no attachments and local engine can solve it, and either API key is missing or offline
        if (attachments.isEmpty() && trimmedQuestion.isNotEmpty() && apiKey.isEmpty()) {
            val localSol = LocalMathEngine.solveLocally(trimmedQuestion)
            if (localSol != null) {
                return@withContext Result.success(localSol)
            }
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please tap the Key icon at the top to enter your API key or configure Secrets in AI Studio.")
            )
        }

        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please tap the Key icon at the top to enter your API key or configure Secrets in AI Studio.")
            )
        }

        // Try primary model then fallback models if needed
        val candidateModels = listOf("gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-pro-preview")
        var lastException: Exception? = null

        for (model in candidateModels) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val payload = buildPromptPayload(trimmedQuestion, attachments)
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = payload.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    val errorMsg = parseErrorMessage(response.code, responseBody)
                    Log.w("GeminiMathService", "Model $model returned error: $errorMsg, code: ${response.code}")
                    // If error is authentication/quota (401, 403, 429), don't retry same key on different model pointlessly
                    if (response.code in listOf(401, 403)) {
                        return@withContext Result.failure(Exception(errorMsg))
                    }
                    lastException = Exception(errorMsg)
                    continue
                }

                val solution = parseGeminiResponse(trimmedQuestion, responseBody)
                return@withContext Result.success(solution)
            } catch (e: Exception) {
                Log.w("GeminiMathService", "Call failed on model $model", e)
                lastException = e
            }
        }

        // If online call failed on all models and we have a text query, attempt local engine fallback
        if (trimmedQuestion.isNotEmpty()) {
            val localSol = LocalMathEngine.solveLocally(trimmedQuestion)
            if (localSol != null) {
                return@withContext Result.success(localSol)
            }
        }

        Result.failure(lastException ?: Exception("Failed to solve problem. Please check internet connection or retry."))
    }

    suspend fun askFollowUp(
        previousSolution: MathSolution,
        followUpQuestion: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("API key missing"))
        }

        val model = "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        try {
            val prompt = """
                Context math problem: "${previousSolution.problemText}"
                Solution summary: "${previousSolution.summary}"
                Final answer: "${previousSolution.finalAnswer}"
                
                User has a follow-up question:
                "$followUpQuestion"
                
                Please answer clearly, concisely, and step-by-step to resolve their doubt. Explain formulas and intermediate steps if requested.
            """.trimIndent()

            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray()
                    partsArray.put(JSONObject().put("text", prompt))
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(url)
                .post(rootJson.toString().toRequestBody(mediaType))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception(parseErrorMessage(response.code, body)))
            }

            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates")
            val text = candidates?.optJSONObject(0)
                ?.optJSONObject("content")
                ?.optJSONArray("parts")
                ?.optJSONObject(0)
                ?.optString("text") ?: "No response generated."

            Result.success(text)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPromptPayload(
        questionText: String,
        attachments: List<AttachedFile>
    ): JSONObject {
        val root = JSONObject()

        val systemInstruction = """
            You are an expert, world-class AI Mathematics and Science Solver.
            Your task is to analyze mathematical, scientific, engineering, arithmetic, calculus, algebra, geometry, physics, or word problems from text, images, photos of notebooks/textbooks, or documents.
            
            You MUST return a clean, structured JSON response with the following schema:
            {
              "problemText": "<extracted or cleaned full problem statement>",
              "topic": "<e.g., Algebra, Calculus, Trigonometry, Geometry, Linear Algebra, Statistics, Physics, Arithmetic>",
              "summary": "<one or two sentence summary of the method and strategy>",
              "steps": [
                {
                  "stepNumber": 1,
                  "title": "<step title, e.g., Identify Given Information & Equation>",
                  "explanation": "<detailed explanation in clear terms>",
                  "mathExpression": "<key formula or mathematical equation for this step>",
                  "subSteps": ["<optional sub-point 1>", "<optional sub-point 2>"]
                }
              ],
              "finalAnswer": "<exact final answer clearly formatted, e.g. x = 4 or 42.5 kg>",
              "keyFormulas": ["<formula 1>", "<formula 2>"],
              "tips": ["<verification tip or common pitfall to avoid>"],
              "confidence": "High"
            }
            
            IMPORTANT:
            1. Ensure all calculations are 100% mathematically rigorous, accurate, and step-by-step.
            2. If an image contains handwritten or printed questions, carefully transcribe the problem accurately into "problemText".
            3. Return ONLY valid JSON or JSON in codeblock. Do not include extraneous chatter outside the JSON.
        """.trimIndent()

        // System Instruction Object
        val sysInstObj = JSONObject().apply {
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemInstruction))
            put("parts", sysParts)
        }
        root.put("systemInstruction", sysInstObj)

        // Contents
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        val partsArray = JSONArray()

        // Attachments (Images, PDFs)
        for (att in attachments) {
            val inlineDataObj = JSONObject().apply {
                put("mimeType", att.mimeType)
                put("data", att.base64Data)
            }
            val partObj = JSONObject().apply {
                put("inlineData", inlineDataObj)
            }
            partsArray.put(partObj)
        }

        // Text prompt
        val userPrompt = if (questionText.isNotBlank()) {
            "Solve the following question step by step:\n$questionText"
        } else {
            "Please solve the math problem shown in the attached image/document step by step."
        }
        partsArray.put(JSONObject().put("text", userPrompt))

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        // Generation Config
        val genConfig = JSONObject().apply {
            put("responseMimeType", "application/json")
            put("temperature", 0.1)
            put("topP", 0.95)
            put("maxOutputTokens", 2048)
        }
        root.put("generationConfig", genConfig)

        return root
    }

    private fun parseGeminiResponse(originalPrompt: String, responseBody: String): MathSolution {
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: throw IllegalStateException("No candidate solutions returned by AI.")
        val firstCandidate = candidates.optJSONObject(0) ?: throw IllegalStateException("Empty AI response.")
        val content = firstCandidate.optJSONObject("content") ?: throw IllegalStateException("No content in response.")
        val parts = content.optJSONArray("parts") ?: throw IllegalStateException("No parts in response.")
        
        var rawText = ""
        for (i in 0 until parts.length()) {
            val part = parts.optJSONObject(i)
            rawText += part?.optString("text") ?: ""
        }

        if (rawText.isBlank()) {
            throw IllegalStateException("Received blank response from Gemini AI.")
        }

        // Try extracting JSON from rawText
        val jsonString = extractJson(rawText)
        return try {
            val json = JSONObject(jsonString)
            val problemText = json.optString("problemText").ifEmpty { originalPrompt.ifEmpty { "Math Problem" } }
            val topic = json.optString("topic", "Mathematics")
            val summary = json.optString("summary", "Step-by-step mathematical solution")
            val finalAnswer = json.optString("finalAnswer", "Solution completed")

            val stepsList = mutableListOf<SolutionStep>()
            val stepsArray = json.optJSONArray("steps")
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    val stepObj = stepsArray.optJSONObject(i) ?: continue
                    val subStepsList = mutableListOf<String>()
                    val subStepsArray = stepObj.optJSONArray("subSteps")
                    if (subStepsArray != null) {
                        for (j in 0 until subStepsArray.length()) {
                            subStepsList.add(subStepsArray.optString(j))
                        }
                    }

                    stepsList.add(
                        SolutionStep(
                            stepNumber = stepObj.optInt("stepNumber", i + 1),
                            title = stepObj.optString("title", "Step ${i + 1}"),
                            explanation = stepObj.optString("explanation", ""),
                            mathExpression = stepObj.optString("mathExpression").takeIf { it.isNotBlank() },
                            subSteps = subStepsList
                        )
                    )
                }
            }

            val keyFormulas = mutableListOf<String>()
            val formulasArray = json.optJSONArray("keyFormulas")
            if (formulasArray != null) {
                for (i in 0 until formulasArray.length()) {
                    keyFormulas.add(formulasArray.optString(i))
                }
            }

            val tips = mutableListOf<String>()
            val tipsArray = json.optJSONArray("tips")
            if (tipsArray != null) {
                for (i in 0 until tipsArray.length()) {
                    tips.add(tipsArray.optString(i))
                }
            }

            val confidence = json.optString("confidence", "High")

            MathSolution(
                problemText = problemText,
                topic = topic,
                summary = summary,
                steps = if (stepsList.isNotEmpty()) stepsList else listOf(SolutionStep(1, "Solution", rawText)),
                finalAnswer = finalAnswer,
                keyFormulas = keyFormulas,
                tips = tips,
                confidence = confidence,
                rawResponse = rawText
            )
        } catch (e: Exception) {
            // Fallback: create a structured solution from raw text
            fallbackParseRawText(originalPrompt, rawText)
        }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) return trimmed

        val codeBlockRegex = "```(?:json)?\\s*([\\s\\S]*?)\\s*```".toRegex()
        val match = codeBlockRegex.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }

        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }

        return trimmed
    }

    private fun fallbackParseRawText(originalPrompt: String, rawText: String): MathSolution {
        val lines = rawText.lines().filter { it.isNotBlank() }
        val steps = mutableListOf<SolutionStep>()
        var stepIndex = 1

        for (line in lines) {
            if (line.startsWith("Step", ignoreCase = true) || line.startsWith("#") || line.matches("^\\d+\\..*".toRegex())) {
                steps.add(
                    SolutionStep(
                        stepNumber = stepIndex++,
                        title = line.replace("#", "").trim(),
                        explanation = ""
                    )
                )
            } else if (steps.isNotEmpty()) {
                val lastStep = steps.last()
                val updatedExplanation = if (lastStep.explanation.isBlank()) line else "${lastStep.explanation}\n$line"
                steps[steps.size - 1] = lastStep.copy(explanation = updatedExplanation)
            }
        }

        if (steps.isEmpty()) {
            steps.add(
                SolutionStep(
                    stepNumber = 1,
                    title = "Step-by-Step Solution",
                    explanation = rawText
                )
            )
        }

        val finalAnswerLine = lines.findLast {
            it.contains("Answer", ignoreCase = true) || it.contains("Result", ignoreCase = true) || it.contains("=")
        } ?: lines.lastOrNull() ?: "Solved"

        return MathSolution(
            problemText = originalPrompt.ifBlank { "Math Problem" },
            topic = "General Mathematics",
            summary = "Step-by-step solution provided by Gemini AI",
            steps = steps,
            finalAnswer = finalAnswerLine.replace("Answer:", "").replace("**", "").trim(),
            rawResponse = rawText
        )
    }

    private fun parseErrorMessage(statusCode: Int, responseBody: String): String {
        return try {
            val root = JSONObject(responseBody)
            val errorObj = root.optJSONObject("error")
            val message = errorObj?.optString("message") ?: "HTTP error $statusCode"
            when (statusCode) {
                400 -> "Invalid request: $message"
                401, 403 -> "Authentication error: Please check your Gemini API key ($message)"
                429 -> "Rate limit reached: Gemini API is temporarily busy. Please wait a moment and try again."
                500, 503 -> "Gemini AI server is currently overloaded. Please retry."
                else -> message
            }
        } catch (e: Exception) {
            "HTTP error $statusCode: $responseBody"
        }
    }
}
