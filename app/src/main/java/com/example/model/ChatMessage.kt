package com.example.model

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val subject: StudySubject = StudySubject.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

enum class MessageSender {
    USER,
    STUDY_MATE
}
