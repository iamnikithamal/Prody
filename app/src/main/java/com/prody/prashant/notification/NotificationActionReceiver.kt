package com.prody.prashant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver that handles notification action button clicks.
 */
class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_DISMISS -> {
                // Dismiss the notification
                NotificationHelper.cancelNotification(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
            }
            ACTION_SNOOZE -> {
                // Snooze for 30 minutes
                val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
                NotificationHelper.cancelNotification(context, notificationId)
                NotificationScheduler.snoozeNotification(context, notificationId, 30)
            }
            ACTION_OPEN_LEARN -> {
                // Open learn screen
                NotificationHelper.cancelNotification(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
                // Deep link handled by MainActivity
            }
            ACTION_OPEN_JOURNAL -> {
                // Open journal screen
                NotificationHelper.cancelNotification(context, intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0))
                // Deep link handled by MainActivity
            }
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.prody.prashant.notification.DISMISS"
        const val ACTION_SNOOZE = "com.prody.prashant.notification.SNOOZE"
        const val ACTION_OPEN_LEARN = "com.prody.prashant.notification.OPEN_LEARN"
        const val ACTION_OPEN_JOURNAL = "com.prody.prashant.notification.OPEN_JOURNAL"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
