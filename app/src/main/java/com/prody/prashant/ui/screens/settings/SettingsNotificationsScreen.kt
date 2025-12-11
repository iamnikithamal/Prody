package com.prody.prashant.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import kotlinx.coroutines.launch

/**
 * Notification settings screen for managing reminders and alerts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNotificationsScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }
    val scope = rememberCoroutineScope()

    val notificationsEnabled by app.preferencesManager.notificationsEnabled
        .collectAsStateWithLifecycle(initialValue = true)
    val dailyReminderEnabled by app.preferencesManager.dailyReminderEnabled
        .collectAsStateWithLifecycle(initialValue = true)
    val dailyReminderTime by app.preferencesManager.dailyReminderTime
        .collectAsStateWithLifecycle(
            initialValue = com.prody.prashant.data.local.PreferencesManager.ReminderTime(9, 0)
        )
    val wisdomNotificationsEnabled by app.preferencesManager.wisdomNotificationsEnabled
        .collectAsStateWithLifecycle(initialValue = true)
    val streakRemindersEnabled by app.preferencesManager.streakRemindersEnabled
        .collectAsStateWithLifecycle(initialValue = true)

    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
            // Master Toggle
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Enable Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Master toggle for all notifications",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    app.preferencesManager.setNotificationsEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // Daily Reminder Section
            item {
                SettingsSectionHeader(title = "Daily Reminder")
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.Alarm,
                    title = "Daily Learning Reminder",
                    subtitle = "Remind me to practice every day",
                    checked = dailyReminderEnabled && notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.preferencesManager.setDailyReminderEnabled(enabled)
                        }
                    }
                )
            }

            item {
                ListItem(
                    headlineContent = { Text("Reminder Time") },
                    supportingContent = {
                        Text(
                            text = formatTime(dailyReminderTime.hour, dailyReminderTime.minute),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        TextButton(
                            onClick = { showTimePicker = true },
                            enabled = dailyReminderEnabled && notificationsEnabled
                        ) {
                            Text("Change")
                        }
                    }
                )
            }

            // Other Notifications
            item {
                SettingsSectionHeader(title = "Other Notifications")
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.AutoAwesome,
                    title = "Daily Wisdom",
                    subtitle = "Receive inspirational quotes and wisdom",
                    checked = wisdomNotificationsEnabled && notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.preferencesManager.setWisdomNotificationsEnabled(enabled)
                        }
                    }
                )
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Streak Reminders",
                    subtitle = "Get reminded when your streak is at risk",
                    checked = streakRemindersEnabled && notificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            app.preferencesManager.setStreakRemindersEnabled(enabled)
                        }
                    }
                )
            }

            // Info Card
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Notifications help you maintain consistent learning habits. " +
                                "You can always adjust these settings later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = dailyReminderTime.hour,
            initialMinute = dailyReminderTime.minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                scope.launch {
                    app.preferencesManager.setDailyReminderTime(hour, minute)
                }
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "%d:%02d %s".format(displayHour, minute, period)
}
