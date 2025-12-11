package com.prody.prashant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

/**
 * Receiver that handles notification action button clicks.
 * Handles snooze, dismiss, and navigation actions from notifications.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("NotificationActionReceiver received action: ${intent.action}")

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        when (intent.action) {
            ACTION_DISMISS -> {
                Timber.d("Dismissing notification $notificationId")
                NotificationHelper.cancelNotification(context, notificationId)
            }

            ACTION_SNOOZE -> {
                val snoozeDuration = intent.getIntExtra(EXTRA_SNOOZE_DURATION, DEFAULT_SNOOZE_MINUTES)
                Timber.d("Snoozing notification $notificationId for $snoozeDuration minutes")
                NotificationHelper.cancelNotification(context, notificationId)
                NotificationScheduler.snoozeNotification(context, notificationId, snoozeDuration)
            }

            ACTION_OPEN_LEARN -> {
                Timber.d("Opening Learn screen from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_LEARN)
            }

            ACTION_OPEN_JOURNAL -> {
                Timber.d("Opening Journal screen from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_JOURNAL)
            }

            ACTION_OPEN_BUDDHA -> {
                Timber.d("Opening Buddha screen from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_BUDDHA)
            }

            ACTION_OPEN_FUTURE_SELF -> {
                Timber.d("Opening Future Self screen from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                val letterId = intent.getLongExtra(EXTRA_LETTER_ID, -1L)
                launchAppWithDestination(context, DESTINATION_FUTURE_SELF, letterId)
            }

            ACTION_OPEN_STATS -> {
                Timber.d("Opening Stats screen from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_STATS)
            }

            ACTION_QUICK_LEARN -> {
                Timber.d("Starting quick learning session from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_VOCABULARY_PRACTICE)
            }

            ACTION_NEW_JOURNAL -> {
                Timber.d("Opening new journal entry from notification")
                NotificationHelper.cancelNotification(context, notificationId)
                launchAppWithDestination(context, DESTINATION_JOURNAL_NEW)
            }
        }
    }

    /**
     * Launches the main app with a specific destination.
     */
    private fun launchAppWithDestination(context: Context, destination: String, entityId: Long = -1L) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, destination)
            if (entityId != -1L) {
                putExtra(EXTRA_ENTITY_ID, entityId)
            }
        }

        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            Timber.e("Could not get launch intent for package")
        }
    }

    companion object {
        // Action constants
        const val ACTION_DISMISS = "com.prody.prashant.notification.DISMISS"
        const val ACTION_SNOOZE = "com.prody.prashant.notification.SNOOZE"
        const val ACTION_OPEN_LEARN = "com.prody.prashant.notification.OPEN_LEARN"
        const val ACTION_OPEN_JOURNAL = "com.prody.prashant.notification.OPEN_JOURNAL"
        const val ACTION_OPEN_BUDDHA = "com.prody.prashant.notification.OPEN_BUDDHA"
        const val ACTION_OPEN_FUTURE_SELF = "com.prody.prashant.notification.OPEN_FUTURE_SELF"
        const val ACTION_OPEN_STATS = "com.prody.prashant.notification.OPEN_STATS"
        const val ACTION_QUICK_LEARN = "com.prody.prashant.notification.QUICK_LEARN"
        const val ACTION_NEW_JOURNAL = "com.prody.prashant.notification.NEW_JOURNAL"

        // Extra constants
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_SNOOZE_DURATION = "snooze_duration"
        const val EXTRA_DESTINATION = "destination"
        const val EXTRA_ENTITY_ID = "entity_id"
        const val EXTRA_LETTER_ID = "letter_id"

        // Destination constants
        const val DESTINATION_HOME = "home"
        const val DESTINATION_LEARN = "learn"
        const val DESTINATION_JOURNAL = "journal"
        const val DESTINATION_BUDDHA = "buddha"
        const val DESTINATION_STATS = "stats"
        const val DESTINATION_FUTURE_SELF = "future_self"
        const val DESTINATION_VOCABULARY_PRACTICE = "vocabulary_practice"
        const val DESTINATION_JOURNAL_NEW = "journal_new"

        // Default values
        const val DEFAULT_SNOOZE_MINUTES = 30

        /**
         * Creates a PendingIntent for dismissing a notification.
         */
        fun createDismissIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_DISMISS
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 1,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for snoozing a notification.
         */
        fun createSnoozeIntent(context: Context, notificationId: Int, minutes: Int = DEFAULT_SNOOZE_MINUTES): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_SNOOZE_DURATION, minutes)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 2,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for opening the Learn screen.
         */
        fun createOpenLearnIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_OPEN_LEARN
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 3,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for opening the Journal screen.
         */
        fun createOpenJournalIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_OPEN_JOURNAL
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 4,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for opening the Buddha chat screen.
         */
        fun createOpenBuddhaIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_OPEN_BUDDHA
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 5,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for opening a Future Self letter.
         */
        fun createOpenFutureSelfIntent(context: Context, notificationId: Int, letterId: Long = -1L): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_OPEN_FUTURE_SELF
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_LETTER_ID, letterId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 6,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for starting a quick vocabulary practice session.
         */
        fun createQuickLearnIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_QUICK_LEARN
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 7,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        /**
         * Creates a PendingIntent for starting a new journal entry.
         */
        fun createNewJournalIntent(context: Context, notificationId: Int): android.app.PendingIntent {
            val intent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_NEW_JOURNAL
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return android.app.PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 8,
                intent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
