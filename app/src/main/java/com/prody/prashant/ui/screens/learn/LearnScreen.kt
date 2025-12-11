package com.prody.prashant.ui.screens.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.LearningStatus
import com.prody.prashant.data.local.entity.VocabularyEntity
import com.prody.prashant.data.local.entity.VocabularyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnScreen(
    onNavigateToVocabularyList: (String) -> Unit,
    onNavigateToVocabularyDetail: (Long) -> Unit,
    onNavigateToPractice: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val dueForReview by app.vocabularyRepository.observeDueForReview()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val totalCount by app.vocabularyRepository.observeTotalCount()
        .collectAsStateWithLifecycle(initialValue = 0)
    val masteredCount by app.vocabularyRepository.observeCountByStatus(LearningStatus.MASTERED)
        .collectAsStateWithLifecycle(initialValue = 0)
    val learningCount by app.vocabularyRepository.observeCountByStatus(LearningStatus.LEARNING)
        .collectAsStateWithLifecycle(initialValue = 0)

    var wordOfDay by remember { mutableStateOf<VocabularyEntity?>(null) }

    LaunchedEffect(Unit) {
        wordOfDay = app.vocabularyRepository.getWordOfDay()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Learn",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LearningStatCard(
                    label = "Total",
                    value = totalCount,
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    modifier = Modifier.weight(1f)
                )
                LearningStatCard(
                    label = "Learning",
                    value = learningCount,
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1f)
                )
                LearningStatCard(
                    label = "Mastered",
                    value = masteredCount,
                    icon = Icons.Default.Stars,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Word of the Day
        wordOfDay?.let { word ->
            item {
                WordOfDayCard(
                    vocabulary = word,
                    onClick = { onNavigateToVocabularyDetail(word.id) }
                )
            }
        }

        // Practice Card
        if (dueForReview.isNotEmpty()) {
            item {
                PracticeCard(
                    dueCount = dueForReview.size,
                    onClick = onNavigateToPractice
                )
            }
        }

        // Categories
        item {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CategoryCard(
                        icon = Icons.Default.Spellcheck,
                        title = "Words",
                        subtitle = "Vocabulary",
                        onClick = { onNavigateToVocabularyList("word") }
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.FormatQuote,
                        title = "Quotes",
                        subtitle = "Wisdom",
                        onClick = { onNavigateToVocabularyList("quote") }
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.AutoStories,
                        title = "Proverbs",
                        subtitle = "Ancient wisdom",
                        onClick = { onNavigateToVocabularyList("proverb") }
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.Lightbulb,
                        title = "Idioms",
                        subtitle = "Expressions",
                        onClick = { onNavigateToVocabularyList("idiom") }
                    )
                }
                item {
                    CategoryCard(
                        icon = Icons.Default.TextSnippet,
                        title = "Phrases",
                        subtitle = "Sayings",
                        onClick = { onNavigateToVocabularyList("phrase") }
                    )
                }
            }
        }

        // Due for Review Section
        if (dueForReview.isNotEmpty()) {
            item {
                Text(
                    text = "Due for Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            items(dueForReview.take(5)) { vocabulary ->
                VocabularyListItem(
                    vocabulary = vocabulary,
                    onClick = { onNavigateToVocabularyDetail(vocabulary.id) }
                )
            }

            if (dueForReview.size > 5) {
                item {
                    TextButton(
                        onClick = onNavigateToPractice,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    ) {
                        Text("View all ${dueForReview.size} items")
                    }
                }
            }
        }
    }
}

@Composable
private fun LearningStatCard(
    label: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WordOfDayCard(
    vocabulary: VocabularyEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Word of the Day",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = vocabulary.word,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vocabulary.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PracticeCard(
    dueCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Practice Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = "$dueCount items ready for review",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun CategoryCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VocabularyListItem(
    vocabulary: VocabularyEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        when (vocabulary.learningStatus) {
                            LearningStatus.MASTERED -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            LearningStatus.REVIEWING -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (vocabulary.type) {
                        VocabularyType.WORD -> Icons.Default.Spellcheck
                        VocabularyType.QUOTE -> Icons.Default.FormatQuote
                        VocabularyType.PROVERB -> Icons.Default.AutoStories
                        VocabularyType.IDIOM -> Icons.Default.Lightbulb
                        VocabularyType.PHRASE -> Icons.Default.TextSnippet
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vocabulary.word,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = vocabulary.meaning.take(50) + if (vocabulary.meaning.length > 50) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (vocabulary.isFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
