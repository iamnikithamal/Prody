package com.prody.prashant.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.prody.prashant.ProdiApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Receiver that handles device boot completed events.
 * Used to reschedule notifications after device restart.
 */
class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed - rescheduling notifications")

            // Use goAsync() to allow the receiver to run asynchronously
            val pendingResult = goAsync()

            scope.launch {
                try {
                    val app = context.applicationContext as? ProdiApplication
                    if (app == null) {
                        Timber.e("Could not get ProdiApplication in BootReceiver")
                        pendingResult.finish()
                        return@launch
                    }

                    val prefs = app.preferencesManager

                    // Check if notifications are enabled and reschedule accordingly
                    val notificationsEnabled = prefs.notificationsEnabled.first()
                    if (!notificationsEnabled) {
                        Timber.d("Notifications are disabled globally - not rescheduling")
                        pendingResult.finish()
                        return@launch
                    }

                    val dailyReminderEnabled = prefs.dailyReminderEnabled.first()
                    val streakRemindersEnabled = prefs.streakRemindersEnabled.first()
                    val wisdomNotificationsEnabled = prefs.wisdomNotificationsEnabled.first()

                    if (dailyReminderEnabled) {
                        val reminderTime = prefs.dailyReminderTime.first()
                        NotificationScheduler.scheduleDailyReminder(context, reminderTime.hour, reminderTime.minute)
                        Timber.d("Rescheduled daily reminder for ${reminderTime.hour}:${reminderTime.minute}")
                    }

                    if (streakRemindersEnabled) {
                        NotificationScheduler.scheduleStreakReminder(context)
                        Timber.d("Rescheduled streak reminder")
                    }

                    if (wisdomNotificationsEnabled) {
                        NotificationScheduler.scheduleWisdomNotifications(context)
                        Timber.d("Rescheduled wisdom notifications")
                    }

                    Timber.d("Notification rescheduling completed")
                } catch (e: Exception) {
                    Timber.e(e, "Error rescheduling notifications after boot")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
