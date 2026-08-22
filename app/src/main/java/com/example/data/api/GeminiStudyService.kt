package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.QuizQuestion
import com.example.model.StudySubject
import com.example.model.SummaryResult
import com.example.model.TopicExplanation
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiStudyService {
    private val tag = "GeminiStudyService"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api: GeminiApiService = retrofit.create(GeminiApiService::class.java)

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    private suspend fun callGemini(
        prompt: String,
        systemInstructionText: String? = null,
        history: List<ChatMessage> = emptyList(),
        temperature: Float = 0.7f,
        responseMimeType: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(tag, "GEMINI_API_KEY is not set or placeholder.")
        }

        val contents = mutableListOf<Content>()
        // Append history (limit to last 6 messages to keep context concise)
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == MessageSender.USER) "user" else "model"
            contents.add(
                Content(
                    role = role,
                    parts = listOf(Part(text = msg.text))
                )
            )
        }
        // Append current prompt
        contents.add(
            Content(
                role = "user",
                parts = listOf(Part(text = prompt))
            )
        )

        val systemInstruction = systemInstructionText?.let {
            Content(
                role = "system",
                parts = listOf(Part(text = it))
            )
        }

        val request = GeminiRequest(
            contents = contents,
            generationConfig = GenerationConfig(
                temperature = temperature,
                responseMimeType = responseMimeType
            ),
            systemInstruction = systemInstruction
        )

        try {
            val response = api.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.failure(Exception("AI returned an empty response. Please try again."))
            }
        } catch (e: Exception) {
            Log.e(tag, "Gemini API error: ${e.message}", e)
            val friendlyMessage = when {
                e.message?.contains("403") == true || e.message?.contains("API_KEY_INVALID") == true ->
                    "API Key is invalid or not yet configured. Please check your Gemini API key in AI Studio Secrets."
                e.message?.contains("429") == true || e.message?.contains("RESOURCE_EXHAUSTED") == true ->
                    "Quota limit reached. Please wait a moment and try again."
                e.message?.contains("Unable to resolve host") == true || e.message?.contains("timeout") == true ->
                    "Network error. Please check your internet connection and try again."
                else ->
                    "StudyMate AI encountered an issue: ${e.localizedMessage ?: "Unable to complete request"}"
            }
            Result.failure(Exception(friendlyMessage))
        }
    }

    /**
     * Feature 1: AI Study Chat
     */
    suspend fun askChat(
        subject: StudySubject,
        history: List<ChatMessage>,
        question: String
    ): Result<String> {
        val systemPrompt = """
            You are StudyMate AI, an encouraging, patient, and knowledgeable educational tutor for school students.
            Subject focus: ${subject.promptPrefix}.
            Guidelines:
            1. Explain concepts in simple, easy-to-understand student-friendly language.
            2. Break down complex ideas into step-by-step points where helpful.
            3. Use friendly formatting (bullet points, bold highlights).
            4. Keep answers concise, clear, and educational.
            5. Encourage curiosity and critical thinking.
        """.trimIndent()

        return callGemini(
            prompt = question,
            systemInstructionText = systemPrompt,
            history = history,
            temperature = 0.6f
        )
    }

    /**
     * Feature 2: Explain a Topic
     */
    suspend fun explainTopic(
        subject: StudySubject,
        topic: String
    ): Result<TopicExplanation> {
        val systemPrompt = """
            You are an expert school teacher explaining a concept to a student.
            Subject: ${subject.displayName}.
            Structure your response using these EXACT section headers:
            ### SIMPLE EXPLANATION
            (Provide a clear, 2-4 sentence explanation that any student can understand)

            ### IMPORTANT POINTS
            - (Point 1)
            - (Point 2)
            - (Point 3)
            - (Point 4)

            ### EXAMPLES
            - **Example 1:** (Clear real-world or academic example)
            - **Example 2:** (Another practical example)

            ### QUICK TIP
            (A short mnemonic or memory trick to remember this)
        """.trimIndent()

        val prompt = "Please explain the topic '$topic' in ${subject.displayName} for students."
        val result = callGemini(
            prompt = prompt,
            systemInstructionText = systemPrompt,
            temperature = 0.5f
        )

        return result.mapCatching { text ->
            parseTopicExplanation(topic, subject, text)
        }
    }

    private fun parseTopicExplanation(topic: String, subject: StudySubject, text: String): TopicExplanation {
        var simpleExplanation = ""
        val keyPoints = mutableListOf<String>()
        val examples = mutableListOf<String>()
        var quickTip: String? = null

        val sections = text.split(Regex("###\\s+"))
        for (section in sections) {
            val lines = section.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue
            val header = lines.first().uppercase()
            val contentLines = lines.drop(1)

            when {
                header.contains("SIMPLE EXPLANATION") || header.contains("EXPLANATION") -> {
                    simpleExplanation = contentLines.joinToString("\n").trim()
                }
                header.contains("IMPORTANT POINTS") || header.contains("POINTS") || header.contains("KEY") -> {
                    for (line in contentLines) {
                        val cleaned = line.replace(Regex("^[-*•\\d.]+\\s*"), "").trim()
                        if (cleaned.isNotBlank()) keyPoints.add(cleaned)
                    }
                }
                header.contains("EXAMPLES") || header.contains("EXAMPLE") -> {
                    for (line in contentLines) {
                        val cleaned = line.replace(Regex("^[-*•\\d.]+\\s*"), "").trim()
                        if (cleaned.isNotBlank()) examples.add(cleaned)
                    }
                }
                header.contains("QUICK TIP") || header.contains("TIP") || header.contains("MNEMONIC") -> {
                    quickTip = contentLines.joinToString("\n").trim()
                }
            }
        }

        if (simpleExplanation.isBlank()) {
            simpleExplanation = text.take(300).trim()
        }
        if (keyPoints.isEmpty()) {
            keyPoints.add("Key concepts and principles related to $topic.")
        }
        if (examples.isEmpty()) {
            examples.add("Practical applications and illustrations of $topic.")
        }

        return TopicExplanation(
            topic = topic,
            subject = subject,
            simpleExplanation = simpleExplanation,
            keyPoints = keyPoints,
            examples = examples,
            quickTip = quickTip
        )
    }

    /**
     * Feature 3: Quiz Generator
     */
    suspend fun generateQuiz(
        subject: StudySubject,
        topic: String
    ): Result<List<QuizQuestion>> {
        val systemPrompt = """
            You are an educational quiz generator. Generate a 5-question multiple choice quiz about the topic for students.
            Respond ONLY with valid JSON in this exact structure:
            {
              "questions": [
                {
                  "id": 1,
                  "question": "Question text here?",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctIndex": 0,
                  "explanation": "Short explanation of why this answer is correct."
                }
              ]
            }
            Do not include markdown codeblocks or other text, just valid JSON.
        """.trimIndent()

        val prompt = "Create 5 multiple choice questions on '$topic' in ${subject.displayName}."
        val result = callGemini(
            prompt = prompt,
            systemInstructionText = systemPrompt,
            temperature = 0.4f,
            responseMimeType = "application/json"
        )

        return result.mapCatching { jsonText ->
            parseQuizQuestions(topic, jsonText)
        }
    }

    private fun parseQuizQuestions(topic: String, rawJson: String): List<QuizQuestion> {
        val cleanJson = rawJson.replace("```json", "").replace("```", "").trim()
        try {
            val adapter = moshi.adapter(QuizResponseDto::class.java)
            val dto = adapter.fromJson(cleanJson)
            if (dto != null && dto.questions.isNotEmpty()) {
                return dto.questions.mapIndexed { index, q ->
                    QuizQuestion(
                        id = index + 1,
                        question = q.question,
                        options = if (q.options.size >= 2) q.options else listOf("True", "False"),
                        correctIndex = q.correctIndex.coerceIn(0, (q.options.size - 1).coerceAtLeast(0)),
                        explanation = q.explanation
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "JSON parse error on quiz: ${e.message}")
        }

        // Fallback fallback questions if JSON parsing encountered unexpected format
        return listOf(
            QuizQuestion(
                id = 1,
                question = "What is the primary concept behind $topic?",
                options = listOf(
                    "Fundamental mechanism and definition",
                    "Unrelated secondary factor",
                    "Opposing theoretical viewpoint",
                    "Random hypothesis"
                ),
                correctIndex = 0,
                explanation = "The primary concept describes the foundational definition and principles of $topic."
            ),
            QuizQuestion(
                id = 2,
                question = "Why is understanding $topic important in study?",
                options = listOf(
                    "It has no practical value",
                    "It builds foundational knowledge for solving related problems",
                    "It is only useful for historical records",
                    "It applies only in rare circumstances"
                ),
                correctIndex = 1,
                explanation = "Mastering $topic allows students to comprehend interconnected topics and solve practical problems."
            ),
            QuizQuestion(
                id = 3,
                question = "Which of the following best represents an example of $topic?",
                options = listOf(
                    "A direct real-world manifestation of the rule",
                    "A violation of physical laws",
                    "An undefined constant",
                    "None of the above"
                ),
                correctIndex = 0,
                explanation = "Direct examples illustrate how the rules and observations operate in reality."
            ),
            QuizQuestion(
                id = 4,
                question = "Which method helps in reviewing $topic effectively?",
                options = listOf(
                    "Memorizing without understanding",
                    "Active recall, practice questions, and summarization",
                    "Skipping practice exercises",
                    "Reading only once quickly"
                ),
                correctIndex = 1,
                explanation = "Active recall and practicing questions strengthen long-term retention."
            ),
            QuizQuestion(
                id = 5,
                question = "How are principles of $topic validated?",
                options = listOf(
                    "Through testing, formulas, or evidence",
                    "By guessing",
                    "Ignoring contradictory facts",
                    "Arbitrary consensus"
                ),
                correctIndex = 0,
                explanation = "Educational subjects validate hypotheses and rules through verified evidence and systematic analysis."
            )
        )
    }

    /**
     * Feature 4: Study Summary
     */
    suspend fun summarizeNotes(notes: String): Result<SummaryResult> {
        val systemPrompt = """
            You are an educational study notes organizer.
            Summarize the student's study notes into clean, structured revision material.
            Format your response using these EXACT section headers:
            ### OVERVIEW
            (A concise 2-3 sentence summary of the key subject matter)

            ### KEY BULLET POINTS
            - (Core point 1)
            - (Core point 2)
            - (Core point 3)
            - (Core point 4)
            - (Core point 5)

            ### VOCABULARY & DEFINITIONS
            - **Term 1**: Definition or significance
            - **Term 2**: Definition or significance
        """.trimIndent()

        val prompt = "Please summarize and organize these study notes for fast exam revision:\n\n$notes"
        val result = callGemini(
            prompt = prompt,
            systemInstructionText = systemPrompt,
            temperature = 0.5f
        )

        return result.mapCatching { text ->
            parseSummaryResult(notes, text)
        }
    }

    private fun parseSummaryResult(originalNotes: String, text: String): SummaryResult {
        var summary = ""
        val keyPoints = mutableListOf<String>()
        val vocabulary = mutableListOf<Pair<String, String>>()

        val sections = text.split(Regex("###\\s+"))
        for (section in sections) {
            val lines = section.lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue
            val header = lines.first().uppercase()
            val contentLines = lines.drop(1)

            when {
                header.contains("OVERVIEW") || header.contains("SUMMARY") -> {
                    summary = contentLines.joinToString("\n").trim()
                }
                header.contains("KEY") || header.contains("POINTS") || header.contains("BULLET") -> {
                    for (line in contentLines) {
                        val cleaned = line.replace(Regex("^[-*•\\d.]+\\s*"), "").trim()
                        if (cleaned.isNotBlank()) keyPoints.add(cleaned)
                    }
                }
                header.contains("VOCABULARY") || header.contains("TERMS") || header.contains("DEFINITIONS") -> {
                    for (line in contentLines) {
                        val cleaned = line.replace(Regex("^[-*•\\d.]+\\s*"), "").trim()
                        val parts = cleaned.split(Regex("[:–-]"), limit = 2)
                        if (parts.size == 2) {
                            val term = parts[0].replace("**", "").replace("*", "").trim()
                            val def = parts[1].replace("**", "").replace("*", "").trim()
                            if (term.isNotBlank() && def.isNotBlank()) {
                                vocabulary.add(Pair(term, def))
                            }
                        } else if (cleaned.isNotBlank()) {
                            vocabulary.add(Pair("Concept", cleaned))
                        }
                    }
                }
            }
        }

        if (summary.isBlank()) {
            summary = text.take(250).trim()
        }
        if (keyPoints.isEmpty()) {
            keyPoints.add("Main ideas summarized from student notes.")
        }

        val firstLineTitle = originalNotes.lines().firstOrNull { it.isNotBlank() }?.take(40) ?: "Study Notes Summary"

        return SummaryResult(
            title = firstLineTitle,
            summary = summary,
            keyPoints = keyPoints,
            vocabulary = vocabulary
        )
    }
}
