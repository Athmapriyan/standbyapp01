package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BedsideNote

@Composable
fun WidgetNotes(
    notes: List<BedsideNote>,
    onToggleNote: (BedsideNote) -> Unit,
    modifier: Modifier = Modifier,
    useNightRed: Boolean = false,
    accentColor: Color = Color(0xFFFF5722)
) {
    val themeRed = Color(0xFFFF1E1E)
    val titleColor = if (useNightRed) themeRed else accentColor
    val activeColor = if (useNightRed) themeRed else accentColor
    val completedColor = if (useNightRed) themeRed.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val textColor = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UP NEXT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (useNightRed) themeRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )

            Icon(
                imageVector = Icons.Default.StickyNote2,
                contentDescription = null,
                tint = if (useNightRed) themeRed.copy(alpha = 0.6f) else titleColor.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No bedside tasks.\nAdd them on the dashboard!",
                    fontSize = 10.sp,
                    color = if (useNightRed) themeRed.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(notes.take(4)) { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (useNightRed) Color(0xFF1E0303) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                            )
                            .clickable { onToggleNote(note) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (note.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle task status",
                            tint = if (note.isCompleted) completedColor else activeColor,
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("bedside_note_checkbox_${note.id}")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = note.content,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (note.isCompleted) completedColor else textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
