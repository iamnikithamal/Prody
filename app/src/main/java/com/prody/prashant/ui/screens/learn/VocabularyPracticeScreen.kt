package com.prody.prashant.ui.screens.learn

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.VocabularyEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyPracticeScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()

    var dueItems by remember { mutableStateOf<List<VocabularyEntity>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var completedCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        dueItems = app.vocabularyRepository.getDueForReview(20)
    }

    val currentItem = dueItems.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (dueItems.isNotEmpty()) {
                        Text("${currentIndex + 1} / ${dueItems.size}")
                    } else {
                        Text("Practice")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicator
            if (dueItems.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { (currentIndex.toFloat() + 1) / dueItems.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }

            when {
                dueItems.isEmpty() -> {
                    // No items to review
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "All caught up!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No items due for review",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack) {
                                Text("Back to Learn")
                            }
                        }
                    }
                }

                currentIndex >= dueItems.size -> {
                    // Completed all items
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Practice Complete!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You reviewed ${dueItems.size} items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack) {
                                Text("Done")
                            }
                        }
                    }
                }

                currentItem != null -> {
                    // Practice Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!showAnswer) {
                                // Question side
                                Text(
                                    text = "What does this mean?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = currentItem.word,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                currentItem.author?.let { author ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "— $author",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(
                                    onClick = { showAnswer = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Show Answer")
                                }
                            } else {
                                // Answer side
                                Text(
                                    text = currentItem.word,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = currentItem.meaning,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                currentItem.example?.let { example ->
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "\"$example\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Rating buttons (shown after answer is revealed)
                    AnimatedVisibility(
                        visible = showAnswer,
                        enter = fadeIn() + slideInVertically { it }
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "How well did you know it?",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RatingButton(
                                    text = "Again",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            app.vocabularyRepository.recordReview(currentItem.id, 1)
                                            showAnswer = false
                                            currentIndex++
                                        }
                                    }
                                )
                                RatingButton(
                                    text = "Hard",
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            app.vocabularyRepository.recordReview(currentItem.id, 3)
                                            showAnswer = false
                                            currentIndex++
                                        }
                                    }
                                )
                                RatingButton(
                                    text = "Good",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            app.vocabularyRepository.recordReview(currentItem.id, 4)
                                            showAnswer = false
                                            currentIndex++
                                        }
                                    }
                                )
                                RatingButton(
                                    text = "Easy",
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        scope.launch {
                                            app.vocabularyRepository.recordReview(currentItem.id, 5)
                                            showAnswer = false
                                            currentIndex++
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButton(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium)
    }
}
