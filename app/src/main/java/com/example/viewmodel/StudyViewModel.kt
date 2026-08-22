package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiStudyService
import com.example.model.ChatMessage
import com.example.model.MessageSender
import com.example.model.QuizQuestion
import com.example.model.QuizState
import com.example.model.StudySubject
import com.example.model.SummaryResult
import com.example.model.TopicExplanation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            sender = MessageSender.STUDY_MATE,
            text = "Hi there! 👋 I'm StudyMate AI, your personal study assistant. Pick a subject below or ask me any question about Math, Science, English, or History!",
            subject = StudySubject.GENERAL
        )
    ),
    val selectedSubject: StudySubject = StudySubject.GENERAL,
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class ExplainUiState(
    val topicInput: String = "",
    val selectedSubject: StudySubject = StudySubject.SCIENCE,
    val isLoading: Boolean = false,
    val explanation: TopicExplanation? = null,
    val errorMessage: String? = null
)

data class SummaryUiState(
    val notesInput: String = "",
    val isLoading: Boolean = false,
    val summaryResult: SummaryResult? = null,
    val errorMessage: String? = null
)

class StudyViewModel(
    private val service: GeminiStudyService = GeminiStudyService()
) : ViewModel() {

    // --- Chat State ---
    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chatState.asStateFlow()

    // --- Explain State ---
    private val _explainState = MutableStateFlow(ExplainUiState())
    val explainState: StateFlow<ExplainUiState> = _explainState.asStateFlow()

    // --- Quiz State ---
    private val _quizTopicInput = MutableStateFlow("Photosynthesis")
    val quizTopicInput: StateFlow<String> = _quizTopicInput.asStateFlow()

    private val _quizSubject = MutableStateFlow(StudySubject.SCIENCE)
    val quizSubject: StateFlow<StudySubject> = _quizSubject.asStateFlow()

    private val _quizState = MutableStateFlow(QuizState())
    val quizState: StateFlow<QuizState> = _quizState.asStateFlow()

    // --- Summary State ---
    private val _summaryState = MutableStateFlow(SummaryUiState())
    val summaryState: StateFlow<SummaryUiState> = _summaryState.asStateFlow()

    // ==========================================
    // Feature 1: Chat Actions
    // ==========================================
    fun onChatInputChanged(text: String) {
        _chatState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun onChatSubjectSelected(subject: StudySubject) {
        _chatState.update { it.copy(selectedSubject = subject) }
    }

    fun sendChatMessage() {
        val current = _chatState.value
        val text = current.inputText.trim()
        if (text.isBlank() || current.isLoading) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = text,
            subject = current.selectedSubject
        )

        val updatedMessages = current.messages + userMessage
        _chatState.update {
            it.copy(
                messages = updatedMessages,
                inputText = "",
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = service.askChat(
                subject = current.selectedSubject,
                history = updatedMessages,
                question = text
            )

            result.fold(
                onSuccess = { responseText ->
                    val aiMessage = ChatMessage(
                        sender = MessageSender.STUDY_MATE,
                        text = responseText,
                        subject = current.selectedSubject
                    )
                    _chatState.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Failed to get response"
                    val errorChatMessage = ChatMessage(
                        sender = MessageSender.STUDY_MATE,
                        text = "⚠️ $errorMsg",
                        subject = current.selectedSubject,
                        isError = true
                    )
                    _chatState.update {
                        it.copy(
                            messages = it.messages + errorChatMessage,
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            )
        }
    }

    fun useStarterPrompt(prompt: String, subject: StudySubject) {
        _chatState.update {
            it.copy(
                inputText = prompt,
                selectedSubject = subject
            )
        }
        sendChatMessage()
    }

    fun clearChat() {
        _chatState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        sender = MessageSender.STUDY_MATE,
                        text = "Chat cleared! Ask me another question about any school subject.",
                        subject = it.selectedSubject
                    )
                ),
                errorMessage = null
            )
        }
    }

    // ==========================================
    // Feature 2: Explain Topic Actions
    // ==========================================
    fun onExplainTopicInputChanged(topic: String) {
        _explainState.update { it.copy(topicInput = topic, errorMessage = null) }
    }

    fun onExplainSubjectSelected(subject: StudySubject) {
        _explainState.update { it.copy(selectedSubject = subject) }
    }

    fun generateExplanation() {
        val current = _explainState.value
        val topic = current.topicInput.trim()
        if (topic.isBlank() || current.isLoading) return

        _explainState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = service.explainTopic(
                subject = current.selectedSubject,
                topic = topic
            )

            result.fold(
                onSuccess = { explanation ->
                    _explainState.update {
                        it.copy(
                            explanation = explanation,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _explainState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to explain topic."
                        )
                    }
                }
            )
        }
    }

    fun useSampleTopic(topic: String, subject: StudySubject) {
        _explainState.update {
            it.copy(
                topicInput = topic,
                selectedSubject = subject,
                errorMessage = null
            )
        }
        generateExplanation()
    }

    // ==========================================
    // Feature 3: Quiz Generator Actions
    // ==========================================
    fun onQuizTopicInputChanged(topic: String) {
        _quizTopicInput.value = topic
    }

    fun onQuizSubjectSelected(subject: StudySubject) {
        _quizSubject.value = subject
    }

    fun generateQuiz() {
        val topic = _quizTopicInput.value.trim()
        if (topic.isBlank()) return

        _quizState.update {
            QuizState(
                topic = topic,
                subject = _quizSubject.value,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = service.generateQuiz(
                subject = _quizSubject.value,
                topic = topic
            )

            result.fold(
                onSuccess = { questions ->
                    _quizState.update {
                        it.copy(
                            questions = questions,
                            currentQuestionIndex = 0,
                            selectedAnswers = emptyMap(),
                            isCompleted = false,
                            score = 0,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _quizState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to generate quiz."
                        )
                    }
                }
            )
        }
    }

    fun selectQuizAnswer(optionIndex: Int) {
        val current = _quizState.value
        val currentIndex = current.currentQuestionIndex
        if (current.isCompleted || current.questions.isEmpty()) return

        val updatedAnswers = current.selectedAnswers + (currentIndex to optionIndex)

        // Calculate score if all questions answered or incrementally
        var newScore = 0
        current.questions.forEachIndexed { idx, q ->
            if (updatedAnswers[idx] == q.correctIndex) {
                newScore++
            }
        }

        _quizState.update {
            it.copy(
                selectedAnswers = updatedAnswers,
                score = newScore
            )
        }
    }

    fun nextQuizQuestion() {
        val current = _quizState.value
        if (current.currentQuestionIndex < current.questions.size - 1) {
            _quizState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) }
        } else {
            // Final question submitted -> complete quiz
            _quizState.update { it.copy(isCompleted = true) }
        }
    }

    fun previousQuizQuestion() {
        val current = _quizState.value
        if (current.currentQuestionIndex > 0) {
            _quizState.update { it.copy(currentQuestionIndex = it.currentQuestionIndex - 1) }
        }
    }

    fun retryQuiz() {
        _quizState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedAnswers = emptyMap(),
                isCompleted = false,
                score = 0
            )
        }
    }

    fun resetQuiz() {
        _quizState.update { QuizState() }
    }

    fun useSampleQuizTopic(topic: String, subject: StudySubject) {
        _quizTopicInput.value = topic
        _quizSubject.value = subject
        generateQuiz()
    }

    // ==========================================
    // Feature 4: Study Summary Actions
    // ==========================================
    fun onNotesInputChanged(notes: String) {
        _summaryState.update { it.copy(notesInput = notes, errorMessage = null) }
    }

    fun summarizeNotes() {
        val current = _summaryState.value
        val notes = current.notesInput.trim()
        if (notes.isBlank() || current.isLoading) return

        _summaryState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = service.summarizeNotes(notes)

            result.fold(
                onSuccess = { summaryResult ->
                    _summaryState.update {
                        it.copy(
                            summaryResult = summaryResult,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _summaryState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Failed to summarize notes."
                        )
                    }
                }
            )
        }
    }

    fun loadSampleNotes() {
        val sampleNotes = """
            Photosynthesis Overview:
            Photosynthesis is the chemical process through which green plants, algae, and some bacteria convert light energy into chemical energy stored in glucose molecules.
            
            Chemical Equation:
            6CO₂ (Carbon Dioxide) + 6H₂O (Water) + Light Energy -> C₆H₁₂O₆ (Glucose) + 6O₂ (Oxygen gas).
            
            Two Key Stages:
            1. Light-Dependent Reactions: Occurs in the thylakoid membranes of chloroplasts. Chlorophyll absorbs sunlight and splits water molecules, releasing oxygen and generating ATP and NADPH.
            2. Light-Independent Reactions (Calvin Cycle): Occurs in the stroma. Carbon dioxide is fixed using ATP and NADPH to synthesize glucose.
            
            Key Factors Affecting Rate:
            - Light intensity
            - Carbon dioxide concentration
            - Temperature (enzyme optimum around 25°C - 35°C).
        """.trimIndent()

        _summaryState.update {
            it.copy(
                notesInput = sampleNotes,
                errorMessage = null
            )
        }
    }

    fun clearSummary() {
        _summaryState.update {
            it.copy(
                notesInput = "",
                summaryResult = null,
                errorMessage = null
            )
        }
    }
}
