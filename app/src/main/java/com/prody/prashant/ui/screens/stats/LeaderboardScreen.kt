package com.prody.prashant.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.LevelTitle

/**
 * Leaderboard screen showing user's ranking (local-only for now).
 * In a local-first app, this shows progress against personal bests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val userStats by app.userProgressRepository.observeUserStats()
        .collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Your Position Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = userStats?.levelTitle?.displayName ?: "Novice",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Level ${userStats?.level ?: 1}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Level Progression
            item {
                Text(
                    text = "Level Progression",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Level Tiers
            itemsIndexed(LevelTitle.entries) { index, levelTitle ->
                val isCurrentLevel = userStats?.levelTitle == levelTitle
                val isAchieved = (userStats?.totalXp ?: 0) >= levelTitle.minXp

                LevelTierCard(
                    position = index + 1,
                    levelTitle = levelTitle,
                    isCurrentLevel = isCurrentLevel,
                    isAchieved = isAchieved,
                    currentXp = userStats?.totalXp ?: 0
                )
            }

            // Motivational Footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"The journey of a thousand miles begins with a single step.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "- Lao Tzu",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelTierCard(
    position: Int,
    levelTitle: LevelTitle,
    isCurrentLevel: Boolean,
    isAchieved: Boolean,
    currentXp: Int
) {
    val tierColor = when (position) {
        1 -> Color(0xFFCD7F32) // Bronze
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFFFD700) // Gold
        4 -> Color(0xFFE5E4E2) // Platinum
        5 -> Color(0xFFB9F2FF) // Diamond
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentLevel)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isCurrentLevel)
            CardDefaults.outlinedCardBorder().copy(width = 2.dp)
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isAchieved) tierColor.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isAchieved) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "$position",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = levelTitle.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCurrentLevel) FontWeight.Bold else FontWeight.SemiBold
                    )
                    if (isCurrentLevel) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "YOU",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${levelTitle.minXp}+ XP required",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isCurrentLevel && levelTitle.ordinal < LevelTitle.entries.size - 1) {
                    val nextLevel = LevelTitle.entries[levelTitle.ordinal + 1]
                    val progressToNext = (currentXp - levelTitle.minXp).toFloat() /
                        (nextLevel.minXp - levelTitle.minXp)

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progressToNext.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "${nextLevel.minXp - currentXp} XP to ${nextLevel.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tier Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tierColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (position) {
                        1 -> Icons.Default.Star
                        2 -> Icons.Default.Star
                        3 -> Icons.Default.Star
                        4 -> Icons.Default.Diamond
                        5 -> Icons.Default.Hexagon
                        else -> Icons.Default.Circle
                    },
                    contentDescription = null,
                    tint = tierColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
