package com.example.model

data class SummaryResult(
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val vocabulary: List<Pair<String, String>> = emptyList()
)
