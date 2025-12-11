package com.prody.prashant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver that handles device boot completed events.
 * Used to reschedule notifications after device restart.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all notifications after boot
            NotificationScheduler.rescheduleAllNotifications(context)
        }
    }
}
