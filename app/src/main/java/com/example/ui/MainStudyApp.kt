package com.example.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ExplainScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SummaryScreen
import com.example.viewmodel.StudyViewModel

enum class StudyTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    CHAT("Chat", Icons.AutoMirrored.Filled.Chat, Icons.Outlined.Chat, "tab_chat"),
    EXPLAIN("Explain", Icons.Default.Lightbulb, Icons.Outlined.Lightbulb, "tab_explain"),
    QUIZ("Quiz", Icons.Default.Quiz, Icons.Outlined.Quiz, "tab_quiz"),
    SUMMARY("Summary", Icons.Default.Description, Icons.Outlined.Description, "tab_summary")
}

@Composable
fun MainStudyApp(
    viewModel: StudyViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(StudyTab.CHAT) }

    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val explainState by viewModel.explainState.collectAsStateWithLifecycle()
    val quizTopicInput by viewModel.quizTopicInput.collectAsStateWithLifecycle()
    val quizSubject by viewModel.quizSubject.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val summaryState by viewModel.summaryState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                StudyTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            StudyTab.CHAT -> {
                ChatScreen(
                    state = chatState,
                    onInputChanged = viewModel::onChatInputChanged,
                    onSendClicked = viewModel::sendChatMessage,
                    onSubjectSelected = viewModel::onChatSubjectSelected,
                    onStarterPromptClicked = viewModel::useStarterPrompt,
                    onClearChatClicked = viewModel::clearChat,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            StudyTab.EXPLAIN -> {
                ExplainScreen(
                    state = explainState,
                    onTopicInputChanged = viewModel::onExplainTopicInputChanged,
                    onSubjectSelected = viewModel::onExplainSubjectSelected,
                    onExplainClicked = viewModel::generateExplanation,
                    onSampleTopicClicked = viewModel::useSampleTopic,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            StudyTab.QUIZ -> {
                QuizScreen(
                    quizTopicInput = quizTopicInput,
                    quizSubject = quizSubject,
                    quizState = quizState,
                    onTopicInputChanged = viewModel::onQuizTopicInputChanged,
                    onSubjectSelected = viewModel::onQuizSubjectSelected,
                    onGenerateQuizClicked = viewModel::generateQuiz,
                    onAnswerSelected = viewModel::selectQuizAnswer,
                    onNextQuestionClicked = viewModel::nextQuizQuestion,
                    onPreviousQuestionClicked = viewModel::previousQuizQuestion,
                    onRetryQuizClicked = viewModel::retryQuiz,
                    onResetQuizClicked = viewModel::resetQuiz,
                    onSampleTopicClicked = viewModel::useSampleQuizTopic,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            StudyTab.SUMMARY -> {
                SummaryScreen(
                    state = summaryState,
                    onNotesInputChanged = viewModel::onNotesInputChanged,
                    onSummarizeClicked = viewModel::summarizeNotes,
                    onSampleNotesClicked = viewModel::loadSampleNotes,
                    onClearClicked = viewModel::clearSummary,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}
