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
     * Shows the daily learning reminder notification.
     */
    fun showDailyReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "learn")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to Learn!")
            .setContentText("Dedicate a few minutes to expand your mind today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Start Learning",
                pendingIntent
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_DAILY_REMINDER, notification)
    }

    /**
     * Shows streak at risk notification.
     */
    fun showStreakReminderNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Your Streak is at Risk!")
            .setContentText("Don't break your learning streak! Open Prodi now.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_STREAK_WARNING, notification)
    }

    /**
     * Shows daily wisdom notification.
     */
    fun showWisdomNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        // Sample wisdom quotes - in production, fetch from database
        val wisdoms = listOf(
            "\"The only way to make sense out of change is to plunge into it.\" - Alan Watts",
            "\"Peace comes from within. Do not seek it without.\" - Buddha",
            "\"We suffer more often in imagination than in reality.\" - Seneca",
            "\"The privilege of a lifetime is to become who you truly are.\" - Carl Jung",
            "\"Muddy water is best cleared by leaving it alone.\" - Alan Watts"
        )

        val wisdom = wisdoms.random()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "learn")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WISDOM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Daily Wisdom")
            .setContentText(wisdom)
            .setStyle(NotificationCompat.BigTextStyle().bigText(wisdom))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_WISDOM, notification)
    }

    /**
     * Shows future self letter available notification.
     */
    fun showFutureSelfNotification(context: Context, letterTitle: String) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("destination", "future_self")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_FUTURE_SELF)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("A Letter from Your Past Self")
            .setContentText("\"$letterTitle\" is now available to read!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
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
