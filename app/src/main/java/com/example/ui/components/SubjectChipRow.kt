package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.StudySubject

@Composable
fun SubjectChipRow(
    selectedSubject: StudySubject,
    onSubjectSelected: (StudySubject) -> Unit,
    modifier: Modifier = Modifier,
    includeGeneral: Boolean = true
) {
    val scrollState = rememberScrollState()
    val subjects = if (includeGeneral) {
        StudySubject.values().toList()
    } else {
        StudySubject.values().filter { it != StudySubject.GENERAL }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        subjects.forEach { subject ->
            val isSelected = subject == selectedSubject
            FilterChip(
                selected = isSelected,
                onClick = { onSubjectSelected(subject) },
                label = {
                    Text(
                        text = subject.displayName,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = subject.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else subject.badgeColor
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag("subject_chip_${subject.name.lowercase()}")
            )
        }
    }
}

