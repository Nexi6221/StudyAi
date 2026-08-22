package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuizQuestion
import com.example.model.QuizState
import com.example.model.StudySubject
import com.example.ui.components.StudyHeader
import com.example.ui.components.SubjectChipRow
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.ErrorRedContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer

@Composable
fun QuizScreen(
    quizTopicInput: String,
    quizSubject: StudySubject,
    quizState: QuizState,
    onTopicInputChanged: (String) -> Unit,
    onSubjectSelected: (StudySubject) -> Unit,
    onGenerateQuizClicked: () -> Unit,
    onAnswerSelected: (Int) -> Unit,
    onNextQuestionClicked: () -> Unit,
    onPreviousQuestionClicked: () -> Unit,
    onRetryQuizClicked: () -> Unit,
    onResetQuizClicked: () -> Unit,
    onSampleTopicClicked: (String, StudySubject) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val sampleQuizTopics = when (quizSubject) {
        StudySubject.MATH -> listOf("Fractions & Decimals", "Basic Algebra", "Geometry Angles")
        StudySubject.SCIENCE -> listOf("Solar System", "Human Circulatory System", "States of Matter")
        StudySubject.ENGLISH -> listOf("Parts of Speech", "Grammar Rules", "Literary Devices")
        StudySubject.HISTORY -> listOf("Ancient Rome", "American Revolution", "The Silk Road")
        StudySubject.GENERAL -> listOf("Photosynthesis", "World Geography", "Basic Physics")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StudyHeader(
            title = "Quiz Generator",
            subtitle = "Test your knowledge with 5 customized AI multiple-choice questions"
        )

        // Show topic setup if no quiz is currently active or completed
        if (quizState.questions.isEmpty() && !quizState.isLoading) {
            SubjectChipRow(
                selectedSubject = quizSubject,
                onSubjectSelected = onSubjectSelected,
                includeGeneral = false
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "What subject or topic would you like to quiz?",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = quizTopicInput,
                            onValueChange = onTopicInputChanged,
                            placeholder = {
                                Text("e.g. ${sampleQuizTopics.firstOrNull() ?: "Photosynthesis"}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_topic_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Quick Quiz Presets:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(sampleQuizTopics) { topic ->
                                SuggestionChip(
                                    onClick = { onSampleTopicClicked(topic, quizSubject) },
                                    label = { Text(topic, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onGenerateQuizClicked,
                            enabled = quizTopicInput.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_quiz_button"),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate 5-Question Quiz")
                        }
                    }
                }

                if (quizState.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ ${quizState.errorMessage}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        } else if (quizState.isLoading) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Generating 5 quiz questions on '${quizTopicInput}'...",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "StudyMate AI is drafting questions and explanations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (quizState.isCompleted) {
            // Final Score Screen
            QuizScoreScreen(
                state = quizState,
                onRetry = onRetryQuizClicked,
                onNewQuiz = onResetQuizClicked
            )
        } else {
            // Active Quiz Question View (One question at a time)
            val currentQuestion = quizState.questions.getOrNull(quizState.currentQuestionIndex)
            if (currentQuestion != null) {
                ActiveQuizQuestionView(
                    question = currentQuestion,
                    currentIndex = quizState.currentQuestionIndex,
                    totalQuestions = quizState.questions.size,
                    selectedOptionIndex = quizState.selectedAnswers[quizState.currentQuestionIndex],
                    onSelectAnswer = onAnswerSelected,
                    onNext = onNextQuestionClicked,
                    onPrevious = onPreviousQuestionClicked,
                    onCancel = onResetQuizClicked
                )
            }
        }
    }
}

@Composable
fun ActiveQuizQuestionView(
    question: QuizQuestion,
    currentIndex: Int,
    totalQuestions: Int,
    selectedOptionIndex: Int?,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = (currentIndex + 1).toFloat() / totalQuestions.toFloat()
    val isAnswered = selectedOptionIndex != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Progress bar & counter
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Question ${currentIndex + 1} of $totalQuestions",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${((progress) * 100).toInt()}% Completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Question Card
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Options List (A, B, C, D)
        Text(
            text = "Select your answer:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val optionLetters = listOf("A", "B", "C", "D")
        question.options.forEachIndexed { optionIdx, optionText ->
            val isSelected = selectedOptionIndex == optionIdx
            val isCorrect = optionIdx == question.correctIndex
            val showFeedback = isAnswered

            val borderColor = when {
                showFeedback && isCorrect -> SuccessGreen
                showFeedback && isSelected && !isCorrect -> ErrorRed
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            }

            val containerColor = when {
                showFeedback && isCorrect -> SuccessGreenContainer.copy(alpha = 0.6f)
                showFeedback && isSelected && !isCorrect -> ErrorRedContainer.copy(alpha = 0.6f)
                isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else -> MaterialTheme.colorScheme.surface
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isAnswered) {
                        onSelectAnswer(optionIdx)
                    }
                    .testTag("quiz_option_$optionIdx"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = containerColor),
                border = BorderStroke(if (isSelected || (showFeedback && isCorrect)) 2.dp else 1.dp, borderColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    showFeedback && isCorrect -> SuccessGreen
                                    showFeedback && isSelected && !isCorrect -> ErrorRed
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLetters.getOrElse(optionIdx) { "${optionIdx + 1}" },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected || (showFeedback && isCorrect)) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = optionText,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (showFeedback && isCorrect) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Correct",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (showFeedback && isSelected && !isCorrect) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Incorrect",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Explanation reveal after answer selection
        AnimatedVisibility(visible = isAnswered) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explanation",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentIndex > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
            } else {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Exit Quiz")
                }
            }

            Button(
                onClick = onNext,
                enabled = isAnswered,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("quiz_next_button")
            ) {
                Text(if (currentIndex < totalQuestions - 1) "Next Question" else "See Results")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun QuizScoreScreen(
    state: QuizState,
    onRetry: () -> Unit,
    onNewQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = state.questions.size
    val score = state.score
    val percentage = if (total > 0) (score * 100) / total else 0

    val feedback = when {
        percentage >= 80 -> "Outstanding Job! 🌟 You've mastered this topic!"
        percentage >= 60 -> "Good Effort! 👍 Review the missed questions to improve."
        else -> "Keep Practicing! 💪 A quick review will help you master this."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quiz_score_summary_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (percentage >= 60) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (percentage >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Quiz Completed!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Topic: ${state.topic}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "$score / $total",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "$percentage% Score",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons: Retry and New Quiz
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("retry_quiz_button"),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(imageVector = Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Quiz")
            }

            Button(
                onClick = onNewQuiz,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("new_quiz_button"),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Quiz")
            }
        }

        // Question-by-question Review
        Text(
            text = "Question Review",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        state.questions.forEachIndexed { qIdx, q ->
            val userSelected = state.selectedAnswers[qIdx]
            val isCorrect = userSelected == q.correctIndex

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) SuccessGreenContainer.copy(alpha = 0.3f) else ErrorRedContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, if (isCorrect) SuccessGreen else ErrorRed)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Question ${qIdx + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isCorrect) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCorrect) "Correct" else "Incorrect",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCorrect) SuccessGreen else ErrorRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = q.question,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Correct Answer: ${q.options.getOrElse(q.correctIndex) { "" }}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SuccessGreen
                    )

                    if (!isCorrect && userSelected != null) {
                        Text(
                            text = "Your Answer: ${q.options.getOrElse(userSelected) { "None" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorRed
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Note: ${q.explanation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
