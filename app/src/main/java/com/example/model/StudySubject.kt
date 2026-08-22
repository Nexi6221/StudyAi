package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.EnglishColor
import com.example.ui.theme.GeneralColor
import com.example.ui.theme.HistoryColor
import com.example.ui.theme.MathColor
import com.example.ui.theme.ScienceColor

enum class StudySubject(
    val displayName: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val promptPrefix: String
) {
    GENERAL("All Subjects", Icons.Default.Psychology, GeneralColor, "General School Subject"),
    MATH("Mathematics", Icons.Default.Calculate, MathColor, "Mathematics"),
    SCIENCE("Science", Icons.Default.Science, ScienceColor, "Science (Physics/Chemistry/Biology)"),
    ENGLISH("English", Icons.Default.AutoStories, EnglishColor, "English & Literature"),
    HISTORY("History", Icons.Default.HistoryEdu, HistoryColor, "History & Social Studies")
}
