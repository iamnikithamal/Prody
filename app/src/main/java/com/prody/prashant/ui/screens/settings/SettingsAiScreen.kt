package com.prody.prashant.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prody.prashant.ProdiApplication
import com.prody.prashant.data.local.entity.BuddhaMode
import kotlinx.coroutines.launch

/**
 * AI settings screen for configuring the Buddha AI assistant.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAiScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()

    val geminiApiKey by app.preferencesManager.geminiApiKey
        .collectAsStateWithLifecycle(initialValue = null)
    val geminiModel by app.preferencesManager.geminiModel
        .collectAsStateWithLifecycle(initialValue = "gemini-1.5-flash")
    val buddhaMode by app.preferencesManager.buddhaMode
        .collectAsStateWithLifecycle(initialValue = BuddhaMode.STOIC)
    val aiResponseLength by app.preferencesManager.aiResponseLength
        .collectAsStateWithLifecycle(initialValue = "balanced")

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }
    var showModeDialog by remember { mutableStateOf(false) }
    var showLengthDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buddha AI Settings") },
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
            // API Configuration
            item {
                SettingsSectionHeader(title = "API Configuration")
            }

            item {
                ListItem(
                    headlineContent = { Text("Gemini API Key") },
                    supportingContent = {
                        Text(
                            text = if (geminiApiKey.isNullOrBlank()) "Not configured" else "••••••••${geminiApiKey?.takeLast(4) ?: ""}",
                            color = if (geminiApiKey.isNullOrBlank())
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { showApiKeyDialog = true }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Model") },
                    supportingContent = {
                        Text(
                            text = getModelDisplayName(geminiModel),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { showModelDialog = true }
                )
            }

            // Buddha Personality
            item {
                SettingsSectionHeader(title = "Buddha Personality")
            }

            item {
                ListItem(
                    headlineContent = { Text("Default Mode") },
                    supportingContent = {
                        Text(
                            text = buddhaMode.displayName,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { showModeDialog = true }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Response Length") },
                    supportingContent = {
                        Text(
                            text = aiResponseLength.replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.clickable { showLengthDialog = true }
                )
            }

            // Info Cards
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Getting an API Key",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Visit ai.google.dev\n" +
                                "2. Sign in with your Google account\n" +
                                "3. Create a new API key\n" +
                                "4. Copy and paste it here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Buddha Modes Description
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Buddha Modes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BuddhaMode.entries.forEach { mode ->
                            Text(
                                text = "• ${mode.displayName}: ${mode.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = geminiApiKey ?: "",
            onDismiss = { showApiKeyDialog = false },
            onConfirm = { key ->
                scope.launch {
                    app.preferencesManager.setGeminiApiKey(key.ifBlank { null })
                }
                showApiKeyDialog = false
            }
        )
    }

    // Model Selection Dialog
    if (showModelDialog) {
        ModelSelectionDialog(
            currentModel = geminiModel,
            onDismiss = { showModelDialog = false },
            onSelect = { model ->
                scope.launch {
                    app.preferencesManager.setGeminiModel(model)
                }
                showModelDialog = false
            }
        )
    }

    // Buddha Mode Dialog
    if (showModeDialog) {
        BuddhaModeDialog(
            currentMode = buddhaMode,
            onDismiss = { showModeDialog = false },
            onSelect = { mode ->
                scope.launch {
                    app.preferencesManager.setBuddhaMode(mode)
                }
                showModeDialog = false
            }
        )
    }

    // Response Length Dialog
    if (showLengthDialog) {
        ResponseLengthDialog(
            currentLength = aiResponseLength,
            onDismiss = { showLengthDialog = false },
            onSelect = { length ->
                scope.launch {
                    app.preferencesManager.setAiResponseLength(length)
                }
                showLengthDialog = false
            }
        )
    }
}

@Composable
private fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gemini API Key") },
        text = {
            Column {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    visualTransformation = if (showKey)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showKey) "Hide" else "Show"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your API key is stored locally and never shared.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(apiKey) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ModelSelectionDialog(
    currentModel: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val models = listOf(
        "gemini-1.5-flash" to "Gemini 1.5 Flash (Fast)",
        "gemini-1.5-pro" to "Gemini 1.5 Pro (Advanced)",
        "gemini-2.0-flash-exp" to "Gemini 2.0 Flash (Experimental)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Model") },
        text = {
            Column {
                models.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentModel == id,
                            onClick = { onSelect(id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun BuddhaModeDialog(
    currentMode: BuddhaMode,
    onDismiss: () -> Unit,
    onSelect: (BuddhaMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Buddha Mode") },
        text = {
            Column {
                BuddhaMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onSelect(mode) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = mode.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ResponseLengthDialog(
    currentLength: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val lengths = listOf(
        "concise" to "Concise - Brief, to-the-point responses",
        "balanced" to "Balanced - Moderate detail and context",
        "detailed" to "Detailed - Comprehensive explanations"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Response Length") },
        text = {
            Column {
                lengths.forEach { (id, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentLength == id,
                            onClick = { onSelect(id) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(description)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getModelDisplayName(model: String): String {
    return when (model) {
        "gemini-1.5-flash" -> "Gemini 1.5 Flash"
        "gemini-1.5-pro" -> "Gemini 1.5 Pro"
        "gemini-2.0-flash-exp" -> "Gemini 2.0 Flash"
        else -> model
    }
}
