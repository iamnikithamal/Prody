package com.prody.prashant.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.prody.prashant.MainActivity
import com.prody.prashant.R

/**
 * Helper class for creating and managing notifications.
 */
object NotificationHelper {

    // Notification Channel IDs
    const val CHANNEL_REMINDERS = "reminders"
    const val CHANNEL_WISDOM = "wisdom"
    const val CHANNEL_STREAK = "streak"
    const val CHANNEL_FUTURE_SELF = "future_self"

    // Notification IDs
    const val NOTIFICATION_DAILY_REMINDER = 1001
    const val NOTIFICATION_STREAK_WARNING = 1002
    const val NOTIFICATION_WISDOM = 1003
    const val NOTIFICATION_FUTURE_SELF = 1004

    /**
     * Creates all notification channels for the app.
     * Should be called on app startup.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java)

            // Reminders Channel
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily learning reminders to help you stay consistent"
            }

            // Wisdom Channel
            val wisdomChannel = NotificationChannel(
                CHANNEL_WISDOM,
                "Daily Wisdom",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Inspirational quotes and wisdom throughout the day"
            }

            // Streak Channel
            val streakChannel = NotificationChannel(
                CHANNEL_STREAK,
                "Streak Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when your streak is at risk"
            }

            // Future Self Channel
            val futureSelfChannel = NotificationChannel(
                CHANNEL_FUTURE_SELF,
                "Future Self Letters",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when letters from your past self become available"
            }

            notificationManager.createNotificationChannels(
                listOf(remindersChannel, wisdomChannel, streakChannel, futureSelfChannel)
            )
        }
    }

    /**
     * Shows the daily learning reminder notification with action buttons.
     */
    fun showDailyReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationActionReceiver.EXTRA_DESTINATION, NotificationActionReceiver.DESTINATION_LEARN)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_DAILY_REMINDER, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Get a random motivational message
        val messages = listOf(
            "Dedicate a few minutes to expand your mind today.",
            "'The journey of a thousand miles begins with a single step.' - Lao Tzu",
            "Small daily improvements lead to stunning results.",
            "Your future self will thank you for learning today.",
            "'We are what we repeatedly do.' - Aristotle"
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to Learn!")
            .setContentText(messages.random())
            .setStyle(NotificationCompat.BigTextStyle().bigText(messages.random()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                "Start Learning",
                NotificationActionReceiver.createOpenLearnIntent(context, NOTIFICATION_DAILY_REMINDER)
            )
            .addAction(
                0,
                "Snooze 30m",
                NotificationActionReceiver.createSnoozeIntent(context, NOTIFICATION_DAILY_REMINDER, 30)
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_DAILY_REMINDER, notification)
    }

    /**
     * Shows streak at risk notification with personalized messaging based on streak length.
     */
    fun showStreakReminderNotification(context: Context, currentStreak: Int = 0) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationActionReceiver.EXTRA_DESTINATION, NotificationActionReceiver.DESTINATION_HOME)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_STREAK_WARNING, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create engaging, personalized messages based on streak length
        val (title, message) = when {
            currentStreak >= 90 -> Pair(
                "Legend Status at Risk!",
                "Your $currentStreak-day streak is legendary! Don't let it slip away - masters don't quit."
            )
            currentStreak >= 30 -> Pair(
                "Your Month-Long Journey...",
                "$currentStreak days of dedication. 'The journey of a thousand miles...' - Don't stop now."
            )
            currentStreak >= 14 -> Pair(
                "Two Weeks Strong!",
                "Day $currentStreak is calling. 'We are what we repeatedly do.' - Aristotle"
            )
            currentStreak >= 7 -> Pair(
                "Week Warrior Alert!",
                "You've built $currentStreak days of momentum. Visit today to keep the fire burning."
            )
            currentStreak >= 3 -> Pair(
                "Building Something...",
                "$currentStreak days in - you're forming a habit. 'Small steps, big changes.'"
            )
            else -> Pair(
                "Your Streak Awaits",
                "Don't break the chain! Open Prody to keep your streak alive."
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                "Quick Learn",
                NotificationActionReceiver.createQuickLearnIntent(context, NOTIFICATION_STREAK_WARNING)
            )
            .addAction(
                0,
                "Write Journal",
                NotificationActionReceiver.createNewJournalIntent(context, NOTIFICATION_STREAK_WARNING)
            )
            .addAction(
                0,
                "Snooze 1hr",
                NotificationActionReceiver.createSnoozeIntent(context, NOTIFICATION_STREAK_WARNING, 60)
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_STREAK_WARNING, notification)
    }

    /**
     * Shows daily wisdom notification with philosopher quotes.
     */
    fun showWisdomNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        val wisdomEntry = philosopherWisdoms.random()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationActionReceiver.EXTRA_DESTINATION, NotificationActionReceiver.DESTINATION_LEARN)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_WISDOM, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WISDOM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(wisdomEntry.title)
            .setContentText(wisdomEntry.quote)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("\"${wisdomEntry.quote}\"")
                .setSummaryText("— ${wisdomEntry.author}"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                "Explore",
                NotificationActionReceiver.createOpenLearnIntent(context, NOTIFICATION_WISDOM)
            )
            .addAction(
                0,
                "Ask Buddha",
                NotificationActionReceiver.createOpenBuddhaIntent(context, NOTIFICATION_WISDOM)
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_WISDOM, notification)
    }

    /**
     * Collection of philosopher wisdom for notifications.
     */
    private data class WisdomEntry(
        val title: String,
        val quote: String,
        val author: String
    )

    private val philosopherWisdoms = listOf(
        // Stoic Wisdom
        WisdomEntry(
            "Morning Reflection",
            "When you arise in the morning, think of what a privilege it is to be alive, to think, to enjoy, to love.",
            "Marcus Aurelius"
        ),
        WisdomEntry(
            "On Change",
            "The only way to make sense out of change is to plunge into it, move with it, and join the dance.",
            "Alan Watts"
        ),
        WisdomEntry(
            "On Suffering",
            "We suffer more often in imagination than in reality.",
            "Seneca"
        ),
        WisdomEntry(
            "Inner Strength",
            "You have power over your mind – not outside events. Realize this, and you will find strength.",
            "Marcus Aurelius"
        ),
        WisdomEntry(
            "On Action",
            "Don't explain your philosophy. Embody it.",
            "Epictetus"
        ),
        WisdomEntry(
            "On Time",
            "It is not that we have a short time to live, but that we waste a lot of it.",
            "Seneca"
        ),
        WisdomEntry(
            "Obstacles",
            "The impediment to action advances action. What stands in the way becomes the way.",
            "Marcus Aurelius"
        ),

        // Zen Wisdom
        WisdomEntry(
            "On Clarity",
            "Muddy water is best cleared by leaving it alone.",
            "Alan Watts"
        ),
        WisdomEntry(
            "On Being",
            "This is the real secret of life – to be completely engaged with what you are doing in the here and now.",
            "Alan Watts"
        ),
        WisdomEntry(
            "Present Moment",
            "The present moment is filled with joy and happiness. If you are attentive, you will see it.",
            "Thich Nhat Hanh"
        ),
        WisdomEntry(
            "Beginner's Mind",
            "In the beginner's mind there are many possibilities, but in the expert's there are few.",
            "Shunryu Suzuki"
        ),

        // Jiddu Krishnamurti
        WisdomEntry(
            "Self-Knowledge",
            "The constant assertion of belief is an indication of fear.",
            "Jiddu Krishnamurti"
        ),
        WisdomEntry(
            "On Observation",
            "The observer is the observed.",
            "Jiddu Krishnamurti"
        ),
        WisdomEntry(
            "On Freedom",
            "Freedom is found in the choiceless awareness of our daily existence and activity.",
            "Jiddu Krishnamurti"
        ),

        // Carl Jung
        WisdomEntry(
            "Self-Discovery",
            "The privilege of a lifetime is to become who you truly are.",
            "Carl Jung"
        ),
        WisdomEntry(
            "Inner Journey",
            "Who looks outside, dreams; who looks inside, awakes.",
            "Carl Jung"
        ),
        WisdomEntry(
            "On Consciousness",
            "Until you make the unconscious conscious, it will direct your life and you will call it fate.",
            "Carl Jung"
        ),
        WisdomEntry(
            "Self-Mastery",
            "I am not what happened to me, I am what I choose to become.",
            "Carl Jung"
        ),

        // Buddhist Wisdom
        WisdomEntry(
            "Inner Peace",
            "Peace comes from within. Do not seek it without.",
            "Buddha"
        ),
        WisdomEntry(
            "New Beginnings",
            "Every morning we are born again. What we do today is what matters most.",
            "Buddha"
        ),
        WisdomEntry(
            "Self-Compassion",
            "You yourself, as much as anybody in the entire universe, deserve your love and affection.",
            "Buddha"
        ),
        WisdomEntry(
            "Mind Power",
            "The mind is everything. What you think you become.",
            "Buddha"
        ),

        // Osho
        WisdomEntry(
            "Creativity",
            "Creativity is the greatest rebellion in existence.",
            "Osho"
        ),
        WisdomEntry(
            "Experience",
            "Experience is not what happens to you, it is what you do with what happens to you.",
            "Osho"
        ),

        // Rumi
        WisdomEntry(
            "Healing",
            "The wound is the place where the light enters you.",
            "Rumi"
        ),
        WisdomEntry(
            "Seeking",
            "What you seek is seeking you.",
            "Rumi"
        ),
        WisdomEntry(
            "Transformation",
            "Yesterday I was clever, so I wanted to change the world. Today I am wise, so I am changing myself.",
            "Rumi"
        ),

        // Others
        WisdomEntry(
            "Taking Action",
            "The best time to plant a tree was 20 years ago. The second best time is now.",
            "Chinese Proverb"
        ),
        WisdomEntry(
            "Resilience",
            "Fall seven times, stand up eight.",
            "Japanese Proverb"
        ),
        WisdomEntry(
            "Growth Through Adversity",
            "A smooth sea never made a skilled sailor.",
            "English Proverb"
        ),
        WisdomEntry(
            "On Excellence",
            "We are what we repeatedly do. Excellence is not an act, but a habit.",
            "Aristotle"
        ),
        WisdomEntry(
            "Taking the First Step",
            "A journey of a thousand miles begins with a single step.",
            "Lao Tzu"
        )
    )

    /**
     * Shows future self letter available notification with action buttons.
     */
    fun showFutureSelfNotification(context: Context, letterTitle: String, letterId: Long = -1L) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(NotificationActionReceiver.EXTRA_DESTINATION, NotificationActionReceiver.DESTINATION_FUTURE_SELF)
            putExtra(NotificationActionReceiver.EXTRA_ENTITY_ID, letterId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_FUTURE_SELF, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FUTURE_SELF)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("A Letter from Your Past Self")
            .setContentText("\"$letterTitle\" is now available to read!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your past self has a message for you: \"$letterTitle\"\n\nTake a moment to reflect on what you wrote."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                0,
                "Read Now",
                NotificationActionReceiver.createOpenFutureSelfIntent(context, NOTIFICATION_FUTURE_SELF, letterId)
            )
            .addAction(
                0,
                "Remind Later",
                NotificationActionReceiver.createSnoozeIntent(context, NOTIFICATION_FUTURE_SELF, 60)
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_FUTURE_SELF, notification)
    }

    /**
     * Shows notification after snooze period ends.
     */
    fun showSnoozeEndedNotification(context: Context, originalNotificationId: Int) {
        // Re-show the appropriate notification based on ID
        when (originalNotificationId) {
            NOTIFICATION_DAILY_REMINDER -> showDailyReminderNotification(context)
            NOTIFICATION_STREAK_WARNING -> showStreakReminderNotification(context)
        }
    }

    /**
     * Cancels a specific notification.
     */
    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    /**
     * Cancels all notifications.
     */
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }

    /**
     * Checks if the app has notification permission.
     */
    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
