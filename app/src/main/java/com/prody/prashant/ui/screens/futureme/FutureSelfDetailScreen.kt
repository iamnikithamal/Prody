package com.prody.prashant.ui.screens.futureme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.XpSource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureSelfDetailScreen(
    letterId: Long,
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val letter by app.futureSelfRepository.observeLetterById(letterId)
        .collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    // Mark as opened when viewing delivered letter
    LaunchedEffect(letter) {
        letter?.let {
            if (it.isDelivered && !it.isOpened) {
                app.futureSelfRepository.markLetterAsOpened(it.id)
                app.userProgressRepository.incrementTodayStats(futureLettersOpened = 1)
                app.userProgressRepository.awardXp(
                    XpSource.FUTURE_LETTER_REFLECTED.baseXp,
                    XpSource.FUTURE_LETTER_REFLECTED,
                    "Opened letter from past self"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(letter?.subject ?: "Letter") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        letter?.let { l ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (l.isDelivered)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (l.isDelivered) Icons.Default.MarkEmailRead else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (l.isDelivered)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (l.isDelivered) "Delivered" else "Pending Delivery",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (l.isDelivered)
                                    "Delivered on ${dateFormat.format(Date(l.deliveryDate))}"
                                else
                                    "Will be delivered on ${dateFormat.format(Date(l.deliveryDate))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Written date
                Text(
                    text = "Written on ${dateFormat.format(Date(l.createdAt))}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Letter content
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Dear Future Me,",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = l.content,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                        )
                    }
                }

                // AI Analysis (if available and delivered)
                if (l.isDelivered && l.aiAnalysis != null) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Buddha's Reflection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            l.aiAnalysis?.let { analysis ->
                                Text(
                                    text = analysis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            l.aiEncouragement?.let { encouragement ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = encouragement,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Mood at writing
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Mood when written: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = l.moodAtWriting.emoji + " " + l.moodAtWriting.displayName,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } ?: Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
