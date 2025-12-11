package com.prody.prashant.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.prody.prashant.data.local.entity.DailyActivityEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity calendar screen showing GitHub-style contribution graph.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityCalendarScreen(
    onNavigateBack: () -> Unit
) {
    val app = remember { ProdiApplication.instance }

    // Get date range for last 3 months
    val calendar = Calendar.getInstance()
    val endDate = calendar.timeInMillis
    calendar.add(Calendar.MONTH, -3)
    val startDate = calendar.timeInMillis

    val activities by app.userProgressRepository.observeDailyActivitiesInRange(startDate, endDate)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val totalActiveDays by app.userProgressRepository.observeTotalActiveDays()
        .collectAsStateWithLifecycle(initialValue = 0)

    val userStats by app.userProgressRepository.observeUserStats()
        .collectAsStateWithLifecycle(initialValue = null)

    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Calendar") },
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
            // Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Current Streak",
                        value = "${userStats?.currentStreak ?: 0}",
                        subtitle = "days",
                        icon = Icons.Default.LocalFireDepartment,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Best Streak",
                        value = "${userStats?.longestStreak ?: 0}",
                        subtitle = "days",
                        icon = Icons.Default.EmojiEvents,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Total Active",
                        value = "$totalActiveDays",
                        subtitle = "days",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "This Month",
                        value = "${activities.count { isThisMonth(it.date) }}",
                        subtitle = "days",
                        icon = Icons.Default.Today,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Month Navigation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                if (selectedMonth == 0) {
                                    selectedMonth = 11
                                    selectedYear -= 1
                                } else {
                                    selectedMonth -= 1
                                }
                            }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                            }

                            Text(
                                text = getMonthYearString(selectedMonth, selectedYear),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(onClick = {
                                if (selectedMonth == 11) {
                                    selectedMonth = 0
                                    selectedYear += 1
                                } else {
                                    selectedMonth += 1
                                }
                            }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Day of week headers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Grid
                        CalendarGrid(
                            month = selectedMonth,
                            year = selectedYear,
                            activities = activities
                        )
                    }
                }
            }

            // Activity Legend
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Activity Levels",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Less",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { level ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(getActivityColor(level))
                                )
                            }
                            Text(
                                text = "More",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Recent Activity List
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val recentActivities = activities.sortedByDescending { it.date }.take(7)
            if (recentActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No activity recorded yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Start learning to build your streak!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                recentActivities.forEach { activity ->
                    item {
                        ActivityDayCard(activity = activity)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: Int,
    year: Int,
    activities: List<DailyActivityEntity>
) {
    val daysInMonth = getDaysInMonth(month, year)
    val firstDayOfWeek = getFirstDayOfWeek(month, year)

    val calendarDays = buildList {
        // Add empty cells for days before the first day of the month
        repeat(firstDayOfWeek) { add(null) }
        // Add actual days
        repeat(daysInMonth) { add(it + 1) }
    }

    val rows = calendarDays.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    if (day != null) {
                        val dateMillis = getDateMillis(day, month, year)
                        val activity = activities.find { normalizeDate(it.date) == dateMillis }
                        val activityLevel = activity?.let { calculateActivityLevel(it) } ?: 0f
                        val isToday = isToday(day, month, year)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(getActivityColor(activityLevel))
                                .then(
                                    if (isToday) Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(6.dp)
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (activityLevel > 0.5f)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(36.dp))
                    }
                }
                // Fill remaining cells in the row
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
private fun ActivityDayCard(activity: DailyActivityEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getActivityColor(calculateActivityLevel(activity))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDate(activity.date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buildString {
                        val items = mutableListOf<String>()
                        if (activity.wordsLearned > 0) items.add("${activity.wordsLearned} words")
                        if (activity.journalEntries > 0) items.add("${activity.journalEntries} journal")
                        if (activity.buddhaMessages > 0) items.add("${activity.buddhaMessages} messages")
                        if (items.isEmpty()) items.add("Active")
                        append(items.joinToString(" • "))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${activity.xpEarned}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getActivityColor(level: Float): Color {
    val primary = MaterialTheme.colorScheme.primary
    return when {
        level <= 0f -> MaterialTheme.colorScheme.surfaceVariant
        level < 0.25f -> primary.copy(alpha = 0.25f)
        level < 0.5f -> primary.copy(alpha = 0.5f)
        level < 0.75f -> primary.copy(alpha = 0.75f)
        else -> primary
    }
}

private fun calculateActivityLevel(activity: DailyActivityEntity): Float {
    val maxExpectedXp = 100f
    return (activity.xpEarned / maxExpectedXp).coerceIn(0f, 1f)
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun getFirstDayOfWeek(month: Int, year: Int): Int {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    return calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed (Sunday = 0)
}

private fun getDateMillis(day: Int, month: Int, year: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun normalizeDate(timestamp: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

private fun isToday(day: Int, month: Int, year: Int): Boolean {
    val today = Calendar.getInstance()
    return today.get(Calendar.DAY_OF_MONTH) == day &&
        today.get(Calendar.MONTH) == month &&
        today.get(Calendar.YEAR) == year
}

private fun isThisMonth(timestamp: Long): Boolean {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    calendar.timeInMillis = timestamp
    return calendar.get(Calendar.MONTH) == currentMonth &&
        calendar.get(Calendar.YEAR) == currentYear
}

private fun getMonthYearString(month: Int, year: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    val format = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return format.format(calendar.time)
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    return format.format(Date(timestamp))
}
