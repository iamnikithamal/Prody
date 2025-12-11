package com.prody.prashant.ui.screens.futureme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.XpSource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureSelfNewScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var daysFromNow by remember { mutableIntStateOf(30) }
    var isSaving by remember { mutableStateOf(false) }

    val deliveryDate = remember(daysFromNow) {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysFromNow) }.time
    }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Write to Future Self") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (content.isNotBlank() && !isSaving) {
                                isSaving = true
                                scope.launch {
                                    app.futureSelfRepository.createLetterToFutureSelf(
                                        content = content,
                                        subject = subject.takeIf { it.isNotBlank() },
                                        daysFromNow = daysFromNow
                                    )

                                    // Award XP
                                    app.userProgressRepository.awardXp(
                                        XpSource.FUTURE_LETTER_WRITTEN.baseXp,
                                        XpSource.FUTURE_LETTER_WRITTEN,
                                        "Letter to future self"
                                    )

                                    // Update stats
                                    app.userProgressRepository.incrementTodayStats(futureLettersWritten = 1)

                                    // Check for badge
                                    app.userProgressRepository.updateBadgeProgress("time_traveler", 1)

                                    onSaved()
                                }
                            }
                        },
                        enabled = content.isNotBlank() && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Send", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Delivery date selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Delivery Date",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = dateFormat.format(deliveryDate),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7 to "1 Week", 30 to "1 Month", 90 to "3 Months", 365 to "1 Year").forEach { (days, label) ->
                            FilterChip(
                                selected = daysFromNow == days,
                                onClick = { daysFromNow = days },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subject
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (optional)") },
                placeholder = { Text("A message from your past self...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Letter content
            Text(
                text = "Dear Future Me,",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = {
                    Text("What do you want to tell your future self? Share your hopes, fears, goals, and promises...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Writing prompts
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Prompts",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        "What are you currently struggling with?",
                        "What goals do you want to achieve by then?",
                        "What advice do you have for your future self?",
                        "What do you hope will be different?"
                    ).forEach { prompt ->
                        Text(
                            text = "• $prompt",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
