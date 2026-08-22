package com.example.model

data class TopicExplanation(
    val topic: String,
    val subject: StudySubject,
    val simpleExplanation: String,
    val keyPoints: List<String>,
    val examples: List<String>,
    val quickTip: String? = null
)
