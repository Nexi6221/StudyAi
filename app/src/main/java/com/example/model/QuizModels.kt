package com.example.model

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizState(
    val topic: String = "",
    val subject: StudySubject = StudySubject.GENERAL,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
