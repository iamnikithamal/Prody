package com.prody.prashant.ui.screens.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.prody.prashant.data.local.entity.VocabularyEntity
import com.prody.prashant.data.local.entity.VocabularyType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyListScreen(
    type: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val vocabType = remember(type) {
        when (type.lowercase()) {
            "word" -> VocabularyType.WORD
            "quote" -> VocabularyType.QUOTE
            "proverb" -> VocabularyType.PROVERB
            "idiom" -> VocabularyType.IDIOM
            "phrase" -> VocabularyType.PHRASE
            else -> VocabularyType.WORD
        }
    }

    val vocabularies by app.vocabularyRepository.observeByType(vocabType)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val title = remember(type) {
        when (type.lowercase()) {
            "word" -> "Vocabulary"
            "quote" -> "Quotes"
            "proverb" -> "Proverbs"
            "idiom" -> "Idioms"
            "phrase" -> "Phrases"
            else -> "Vocabulary"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (vocabularies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No ${title.lowercase()} yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vocabularies) { vocab ->
                    VocabularyCard(
                        vocabulary = vocab,
                        onClick = { onNavigateToDetail(vocab.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VocabularyCard(
    vocabulary: VocabularyEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vocabulary.word,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (vocabulary.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = vocabulary.meaning,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            vocabulary.author?.let { author ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— $author",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
