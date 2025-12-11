package com.prody.prashant.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import kotlinx.coroutines.launch

/**
 * Profile edit screen for customizing user profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()

    val userStats by app.userProgressRepository.observeUserStats()
        .collectAsStateWithLifecycle(initialValue = null)

    var displayName by remember(userStats) { mutableStateOf(userStats?.displayName ?: "") }
    var bio by remember(userStats) { mutableStateOf(userStats?.bio ?: "") }
    var selectedAvatar by remember(userStats) { mutableStateOf(userStats?.currentAvatar ?: "default") }
    var selectedBanner by remember(userStats) { mutableStateOf(userStats?.currentBanner ?: "default") }

    var isSaving by remember { mutableStateOf(false) }

    // Available avatars (unlocked ones)
    val unlockedAvatars = remember(userStats) {
        val baseAvatars = listOf("default")
        val unlocked = userStats?.unlockedAvatars?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        (baseAvatars + unlocked).distinct()
    }

    val unlockedBanners = remember(userStats) {
        val baseBanners = listOf("default", "gradient1", "gradient2")
        val unlocked = userStats?.unlockedBanners?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
        (baseBanners + unlocked).distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                app.userProgressRepository.updateProfile(
                                    name = displayName.ifBlank { "Prodi User" },
                                    bio = bio.ifBlank { null }
                                )
                                app.userProgressRepository.updateAvatar(selectedAvatar)
                                app.userProgressRepository.updateBanner(selectedBanner)
                                isSaving = false
                                onSaved()
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        // Banner Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(getBannerGradient(selectedBanner))
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(40.dp))

                            // Avatar Preview
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getAvatarIcon(selectedAvatar),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = displayName.ifBlank { "Prodi User" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (bio.isNotBlank()) {
                                Text(
                                    text = bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Display Name
            item {
                Column {
                    Text(
                        text = "Display Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(30) },
                        placeholder = { Text("Enter your name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words
                        ),
                        supportingText = {
                            Text("${displayName.length}/30")
                        }
                    )
                }
            }

            // Bio
            item {
                Column {
                    Text(
                        text = "Bio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it.take(150) },
                        placeholder = { Text("Tell us about yourself...") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        supportingText = {
                            Text("${bio.length}/150")
                        }
                    )
                }
            }

            // Avatar Selection
            item {
                Column {
                    Text(
                        text = "Avatar",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Unlock more avatars by earning badges",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(unlockedAvatars) { avatarId ->
                            AvatarOption(
                                avatarId = avatarId,
                                isSelected = selectedAvatar == avatarId,
                                onClick = { selectedAvatar = avatarId }
                            )
                        }

                        // Show locked avatars
                        items(getLockedAvatars(unlockedAvatars)) { avatarId ->
                            LockedAvatarOption(avatarId = avatarId)
                        }
                    }
                }
            }

            // Banner Selection
            item {
                Column {
                    Text(
                        text = "Profile Banner",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Customize your profile header",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(unlockedBanners) { bannerId ->
                            BannerOption(
                                bannerId = bannerId,
                                isSelected = selectedBanner == bannerId,
                                onClick = { selectedBanner = bannerId }
                            )
                        }
                    }
                }
            }

            // Daily Goals
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Goals",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Words per day",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${userStats?.dailyGoalWords ?: 5} words",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Journal time",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${userStats?.dailyGoalJournalMinutes ?: 10} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
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
private fun AvatarOption(
    avatarId: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getAvatarIcon(avatarId),
            contentDescription = getAvatarName(avatarId),
            modifier = Modifier.size(40.dp),
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LockedAvatarOption(avatarId: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = getAvatarName(avatarId),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun BannerOption(
    bannerId: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getBannerGradient(bannerId))
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getAvatarIcon(avatarId: String?): ImageVector {
    return when (avatarId) {
        "scholar" -> Icons.Default.School
        "buddha_friend" -> Icons.Default.SelfImprovement
        "philosopher" -> Icons.Default.AutoStories
        "disciplined" -> Icons.Default.MilitaryTech
        "trustworthy" -> Icons.Default.Verified
        else -> Icons.Default.Person
    }
}

private fun getAvatarName(avatarId: String?): String {
    return when (avatarId) {
        "scholar" -> "Scholar"
        "buddha_friend" -> "Buddha's Friend"
        "philosopher" -> "Philosopher"
        "disciplined" -> "Disciplined"
        "trustworthy" -> "Trustworthy"
        else -> "Default"
    }
}

private fun getLockedAvatars(unlockedAvatars: List<String>): List<String> {
    val allAvatars = listOf("scholar", "buddha_friend", "philosopher", "disciplined", "trustworthy")
    return allAvatars.filter { it !in unlockedAvatars }
}

@Composable
private fun getBannerGradient(bannerId: String): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    return when (bannerId) {
        "gradient1" -> Brush.horizontalGradient(listOf(Color(0xFF667eea), Color(0xFF764ba2)))
        "gradient2" -> Brush.horizontalGradient(listOf(Color(0xFF11998e), Color(0xFF38ef7d)))
        "flames" -> Brush.horizontalGradient(listOf(Color(0xFFf12711), Color(0xFFf5af19)))
        "platinum" -> Brush.horizontalGradient(listOf(Color(0xFFbdc3c7), Color(0xFF2c3e50)))
        else -> Brush.horizontalGradient(listOf(primary, tertiary))
    }
}
