package com.prody.prashant.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data management screen for backup, export, and reset functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDataScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val lastBackupTime by app.preferencesManager.lastBackupTime
        .collectAsStateWithLifecycle(initialValue = null)
    val autoBackupEnabled by app.preferencesManager.autoBackupEnabled
        .collectAsStateWithLifecycle(initialValue = false)

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearProgressDialog by remember { mutableStateOf(false) }
    var isExportingJournals by remember { mutableStateOf(false) }
    var isExportingVocabulary by remember { mutableStateOf(false) }
    var isClearingProgress by remember { mutableStateOf(false) }
    var isResettingAll by remember { mutableStateOf(false) }
    var storageInfo by remember { mutableStateOf<StorageInfo?>(null) }

    // Calculate storage info on first composition
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            storageInfo = calculateStorageInfo(app)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Management") },
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
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Backup Section
            item {
                SettingsSectionHeader(title = "Backup")
            }

            item {
                ListItem(
                    headlineContent = { Text("Last Backup") },
                    supportingContent = {
                        Text(
                            text = lastBackupTime?.let { formatDateTime(it) } ?: "Never",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.CloudSync,
                    title = "Auto Backup",
                    subtitle = "Automatically backup data periodically",
                    checked = autoBackupEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.preferencesManager.setAutoBackupEnabled(enabled)
                        }
                    }
                )
            }

            // Export Section
            item {
                SettingsSectionHeader(title = "Export")
            }

            item {
                ListItem(
                    modifier = Modifier.clickable(enabled = !isExportingJournals) {
                        scope.launch {
                            isExportingJournals = true
                            try {
                                exportJournals(context, app)
                            } finally {
                                isExportingJournals = false
                            }
                        }
                    },
                    headlineContent = { Text("Export Journal Entries") },
                    supportingContent = { Text("Export all journal entries as JSON") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (isExportingJournals) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            item {
                ListItem(
                    modifier = Modifier.clickable(enabled = !isExportingVocabulary) {
                        scope.launch {
                            isExportingVocabulary = true
                            try {
                                exportVocabulary(context, app)
                            } finally {
                                isExportingVocabulary = false
                            }
                        }
                    },
                    headlineContent = { Text("Export Vocabulary Progress") },
                    supportingContent = { Text("Export learning progress as JSON") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        if (isExportingVocabulary) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            // Storage Info
            item {
                SettingsSectionHeader(title = "Storage")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "App Data",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = storageInfo?.let { formatFileSize(it.totalSize) } ?: "Calculating...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (storageInfo != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                StorageItem("Journals", storageInfo!!.journalCount, storageInfo!!.journalSize)
                                StorageItem("Vocabulary", storageInfo!!.vocabularyCount, storageInfo!!.vocabularySize)
                                StorageItem("Buddha Chats", storageInfo!!.buddhaCount, storageInfo!!.buddhaSize)
                                StorageItem("Future Letters", storageInfo!!.futureCount, storageInfo!!.futureSize)
                            }
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Data stored locally on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Danger Zone
            item {
                SettingsSectionHeader(title = "Danger Zone")
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Clear Progress",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Reset XP, streaks, and badges",
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            trailingContent = {
                                TextButton(
                                    onClick = { showClearProgressDialog = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Clear")
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )

                        HorizontalDivider()

                        ListItem(
                            headlineContent = {
                                Text(
                                    "Reset All Data",
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            supportingContent = {
                                Text(
                                    "Delete all data and start fresh",
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            trailingContent = {
                                TextButton(
                                    onClick = { showResetDialog = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Reset")
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }

            // Warning Note
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All data is stored locally on your device. " +
                                "Make sure to export your data before resetting if you want to keep it.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Clear Progress Dialog
    if (showClearProgressDialog) {
        AlertDialog(
            onDismissRequest = { showClearProgressDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Clear Progress?") },
            text = {
                Text(
                    "This will reset your XP, level, streaks, and badges. " +
                        "Your journal entries and vocabulary will be preserved. " +
                        "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isClearingProgress = true
                            try {
                                app.userProgressRepository.clearAllProgress()
                                Toast.makeText(context, "Progress cleared successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to clear progress")
                                Toast.makeText(context, "Failed to clear progress", Toast.LENGTH_SHORT).show()
                            } finally {
                                isClearingProgress = false
                                showClearProgressDialog = false
                            }
                        }
                    },
                    enabled = !isClearingProgress,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isClearingProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Clear Progress")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearProgressDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Reset All Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Reset All Data?") },
            text = {
                Text(
                    "This will permanently delete ALL your data including:\n\n" +
                        "• Journal entries\n" +
                        "• Buddha conversations\n" +
                        "• Future self letters\n" +
                        "• Progress and achievements\n" +
                        "• Settings and preferences\n\n" +
                        "This action CANNOT be undone!"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            isResettingAll = true
                            try {
                                // Clear all preferences
                                app.preferencesManager.clearAll()
                                // Clear all database tables
                                app.database.clearAllTables()
                                Toast.makeText(context, "All data deleted. Please restart the app.", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to reset all data")
                                Toast.makeText(context, "Failed to reset data", Toast.LENGTH_SHORT).show()
                            } finally {
                                isResettingAll = false
                                showResetDialog = false
                            }
                        }
                    },
                    enabled = !isResettingAll,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isResettingAll) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete Everything")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}

@Composable
private fun StorageItem(label: String, count: Int, size: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatFileSize(size),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private data class StorageInfo(
    val journalCount: Int,
    val journalSize: Long,
    val vocabularyCount: Int,
    val vocabularySize: Long,
    val buddhaCount: Int,
    val buddhaSize: Long,
    val futureCount: Int,
    val futureSize: Long,
    val totalSize: Long
)

private suspend fun calculateStorageInfo(app: ProdiApplication): StorageInfo {
    val journalCount = app.journalRepository.getTotalJournalCount()
    val vocabularyCount = app.vocabularyRepository.getTotalVocabularyCount()
    val buddhaCount = app.buddhaRepository.getConversationCount()
    val futureCount = app.futureSelfRepository.getTotalLetterCount()

    // Estimate sizes based on average entry sizes
    val journalSize = journalCount * 2048L  // ~2KB per journal entry
    val vocabularySize = vocabularyCount * 512L  // ~512 bytes per vocabulary item
    val buddhaSize = buddhaCount * 4096L  // ~4KB per conversation
    val futureSize = futureCount * 1024L  // ~1KB per letter

    return StorageInfo(
        journalCount = journalCount,
        journalSize = journalSize,
        vocabularyCount = vocabularyCount,
        vocabularySize = vocabularySize,
        buddhaCount = buddhaCount,
        buddhaSize = buddhaSize,
        futureCount = futureCount,
        futureSize = futureSize,
        totalSize = journalSize + vocabularySize + buddhaSize + futureSize
    )
}

private val prettyJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

private suspend fun exportJournals(context: Context, app: ProdiApplication) {
    withContext(Dispatchers.IO) {
        try {
            val journals = app.journalRepository.getAllJournals()
            val jsonContent = prettyJson.encodeToString(journals)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "prody_journals_$timestamp.json"

            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val file = File(exportDir, fileName)
            file.writeText(jsonContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Export Journal Entries"))
            }

            Timber.d("Exported ${journals.size} journal entries")
        } catch (e: Exception) {
            Timber.e(e, "Failed to export journals")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export journals: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun exportVocabulary(context: Context, app: ProdiApplication) {
    withContext(Dispatchers.IO) {
        try {
            val vocabulary = app.vocabularyRepository.getAllVocabulary()
            val jsonContent = prettyJson.encodeToString(vocabulary)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "prody_vocabulary_$timestamp.json"

            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            val file = File(exportDir, fileName)
            file.writeText(jsonContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(Intent.createChooser(shareIntent, "Export Vocabulary Progress"))
            }

            Timber.d("Exported ${vocabulary.size} vocabulary items")
        } catch (e: Exception) {
            Timber.e(e, "Failed to export vocabulary")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Failed to export vocabulary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
