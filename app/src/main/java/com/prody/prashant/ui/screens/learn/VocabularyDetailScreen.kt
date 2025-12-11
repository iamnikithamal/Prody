package com.prody.prashant.ui.screens.learn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.LearningStatus
import com.prody.prashant.data.local.entity.VocabularyEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyDetailScreen(
    vocabularyId: Long,
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val vocabulary by app.vocabularyRepository.observeById(vocabularyId)
        .collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    vocabulary?.let { vocab ->
                        IconButton(
                            onClick = {
                                scope.launch {
                                    app.vocabularyRepository.updateFavoriteStatus(
                                        vocab.id,
                                        !vocab.isFavorite
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (vocab.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (vocab.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        vocabulary?.let { vocab ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Word/Quote
                Text(
                    text = vocab.word,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                // Author (for quotes)
                vocab.author?.let { author ->
                    Text(
                        text = "— $author",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Type and Status badges
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text(vocab.type.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(vocab.learningStatus.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (vocab.learningStatus) {
                                LearningStatus.MASTERED -> MaterialTheme.colorScheme.primaryContainer
                                LearningStatus.REVIEWING -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Meaning
                DetailSection(title = "Meaning") {
                    Text(
                        text = vocab.meaning,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Example
                vocab.example?.let { example ->
                    DetailSection(title = "Example") {
                        Text(
                            text = "\"$example\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // Origin
                vocab.origin?.let { origin ->
                    DetailSection(title = "Origin") {
                        Text(
                            text = origin,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Pronunciation
                vocab.pronunciation?.let { pronunciation ->
                    DetailSection(title = "Pronunciation") {
                        Text(
                            text = pronunciation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Synonyms
                vocab.synonyms?.let { synonyms ->
                    DetailSection(title = "Synonyms") {
                        Text(
                            text = synonyms,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Antonyms
                vocab.antonyms?.let { antonyms ->
                    DetailSection(title = "Antonyms") {
                        Text(
                            text = antonyms,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // AI Mnemonic
                vocab.aiMnemonic?.let { mnemonic ->
                    DetailSection(title = "Memory Trick") {
                        Text(
                            text = mnemonic,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Learning Progress
                DetailSection(title = "Progress") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${vocab.reviewCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Correct",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${vocab.correctCount}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Ease Factor",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "%.2f".format(vocab.easeFactor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Mark as Learned Button
                if (vocab.learningStatus != LearningStatus.MASTERED) {
                    Button(
                        onClick = {
                            scope.launch {
                                app.vocabularyRepository.recordReview(vocab.id, 5)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark as Learned")
                    }
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

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
