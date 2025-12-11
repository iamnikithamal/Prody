package com.prody.prashant.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.prody.prashant.MainActivity
import com.prody.prashant.ProdiApplication
import com.prody.prashant.notification.NotificationActionReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Prodi home screen widget showing daily progress and quick actions.
 */
class ProdiWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 120.dp),  // Small
            DpSize(270.dp, 120.dp),  // Medium
            DpSize(270.dp, 180.dp),  // Large
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Get user stats
        val app = context.applicationContext as? ProdiApplication
        val stats = app?.userProgressRepository?.getUserStats()
        val todayActivity = app?.userProgressRepository?.getTodayActivity()

        val currentStreak = stats?.currentStreak ?: 0
        val totalXp = stats?.totalXp ?: 0
        val level = stats?.level ?: 1
        val levelTitle = stats?.levelTitle?.displayName ?: "Novice"
        val wordsLearnedToday = todayActivity?.wordsLearned ?: 0
        val journalEntriesToday = todayActivity?.journalEntries ?: 0

        // Get random wisdom quote
        val wisdomQuote = getRandomWisdomQuote()

        provideContent {
            ProdiWidgetContent(
                currentStreak = currentStreak,
                totalXp = totalXp,
                level = level,
                levelTitle = levelTitle,
                wordsLearnedToday = wordsLearnedToday,
                journalEntriesToday = journalEntriesToday,
                wisdomQuote = wisdomQuote
            )
        }
    }
}

@Composable
private fun ProdiWidgetContent(
    currentStreak: Int,
    totalXp: Int,
    level: Int,
    levelTitle: String,
    wordsLearnedToday: Int,
    journalEntriesToday: Int,
    wisdomQuote: WisdomQuote
) {
    val size = LocalSize.current

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            when {
                size.width < 200.dp -> SmallWidget(currentStreak, level)
                size.height < 150.dp -> MediumWidget(currentStreak, level, levelTitle, wordsLearnedToday)
                else -> LargeWidget(currentStreak, level, levelTitle, wordsLearnedToday, journalEntriesToday, wisdomQuote)
            }
        }
    }
}

@Composable
private fun SmallWidget(currentStreak: Int, level: Int) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Prody",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.primary
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$currentStreak",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Text(
                    text = "streak",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = GlanceModifier.width(16.dp))

            // Level
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lv.$level",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.primary
                    )
                )
            }
        }
    }
}

@Composable
private fun MediumWidget(
    currentStreak: Int,
    level: Int,
    levelTitle: String,
    wordsLearnedToday: Int
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Streak
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = "$currentStreak",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Text(
                text = "day streak",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        // Divider
        Box(
            modifier = GlanceModifier
                .width(1.dp)
                .height(50.dp)
                .background(GlanceTheme.colors.outline)
        )

        // Right: Level & Today
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = levelTitle,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.primary
                )
            )
            Text(
                text = "Level $level",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "$wordsLearnedToday words today",
                style = TextStyle(
                    fontSize = 10.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun LargeWidget(
    currentStreak: Int,
    level: Int,
    levelTitle: String,
    wordsLearnedToday: Int,
    journalEntriesToday: Int,
    wisdomQuote: WisdomQuote
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header: Prody + Level
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Prody",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "$levelTitle (Lv.$level)",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Stats Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Streak
            StatItem(
                value = "$currentStreak",
                label = "streak",
                modifier = GlanceModifier.defaultWeight()
            )

            // Words
            StatItem(
                value = "$wordsLearnedToday",
                label = "words",
                modifier = GlanceModifier.defaultWeight()
            )

            // Journals
            StatItem(
                value = "$journalEntriesToday",
                label = "journals",
                modifier = GlanceModifier.defaultWeight()
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        // Quote
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(GlanceTheme.colors.surfaceVariant)
                .cornerRadius(8.dp)
                .padding(8.dp)
        ) {
            Column {
                Text(
                    text = "\"${wisdomQuote.quote}\"",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    maxLines = 2
                )
                Text(
                    text = "— ${wisdomQuote.author}",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.primary
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Quick Actions
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuickActionButton(
                label = "Learn",
                destination = NotificationActionReceiver.DESTINATION_LEARN,
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            QuickActionButton(
                label = "Journal",
                destination = NotificationActionReceiver.DESTINATION_JOURNAL_NEW,
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            QuickActionButton(
                label = "Buddha",
                destination = NotificationActionReceiver.DESTINATION_BUDDHA,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    destination: String,
    modifier: GlanceModifier = GlanceModifier
) {
    val intent = Intent(LocalContext.current, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        putExtra(NotificationActionReceiver.EXTRA_DESTINATION, destination)
    }

    Box(
        modifier = modifier
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(8.dp)
            .padding(vertical = 6.dp, horizontal = 8.dp)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
    }
}

// Wisdom quotes for the widget
private data class WisdomQuote(val quote: String, val author: String)

private fun getRandomWisdomQuote(): WisdomQuote {
    val quotes = listOf(
        WisdomQuote("The privilege of a lifetime is to become who you truly are.", "Carl Jung"),
        WisdomQuote("We are what we repeatedly do. Excellence is not an act, but a habit.", "Aristotle"),
        WisdomQuote("The observer is the observed.", "Jiddu Krishnamurti"),
        WisdomQuote("Muddy water is best cleared by leaving it alone.", "Alan Watts"),
        WisdomQuote("We suffer more often in imagination than in reality.", "Seneca"),
        WisdomQuote("The impediment to action advances action.", "Marcus Aurelius"),
        WisdomQuote("What you seek is seeking you.", "Rumi"),
        WisdomQuote("Peace comes from within. Do not seek it without.", "Buddha"),
        WisdomQuote("Creativity is the greatest rebellion in existence.", "Osho"),
        WisdomQuote("A journey of a thousand miles begins with a single step.", "Lao Tzu")
    )
    return quotes.random()
}

/**
 * Widget receiver that handles widget updates.
 */
class ProdiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ProdiWidget()
}
