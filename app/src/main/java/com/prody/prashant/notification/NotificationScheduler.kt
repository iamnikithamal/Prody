package com.prody.prashant.notification

import android.content.Context
import androidx.work.*
import com.prody.prashant.ProdiApplication
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Handles scheduling of various app notifications using WorkManager.
 */
object NotificationScheduler {

    private const val DAILY_REMINDER_WORK = "daily_reminder_work"
    private const val STREAK_REMINDER_WORK = "streak_reminder_work"
    private const val WISDOM_NOTIFICATION_WORK = "wisdom_notification_work"

    /**
     * Schedules the daily learning reminder notification.
     */
    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val workManager = WorkManager.getInstance(context)

        // Cancel existing work
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK)

        val delay = calculateDelayUntilTime(hour, minute)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(DAILY_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels the daily reminder notification.
     */
    fun cancelDailyReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)
    }

    /**
     * Schedules streak risk reminder (sent if user hasn't been active today).
     */
    fun scheduleStreakReminder(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Schedule for evening (8 PM)
        val delay = calculateDelayUntilTime(20, 0)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<StreakReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .addTag(STREAK_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            STREAK_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels streak reminder notifications.
     */
    fun cancelStreakReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(STREAK_REMINDER_WORK)
    }

    /**
     * Schedules random wisdom notifications throughout the day.
     */
    fun scheduleWisdomNotifications(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Send a wisdom notification every 6 hours
        val workRequest = PeriodicWorkRequestBuilder<WisdomNotificationWorker>(
            6, TimeUnit.HOURS
        )
            .setInitialDelay(2, TimeUnit.HOURS) // Start after 2 hours
            .addTag(WISDOM_NOTIFICATION_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WISDOM_NOTIFICATION_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels wisdom notifications.
     */
    fun cancelWisdomNotifications(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WISDOM_NOTIFICATION_WORK)
    }

    /**
     * Snoozes a notification for the specified number of minutes.
     */
    fun snoozeNotification(context: Context, notificationId: Int, minutes: Int) {
        val workRequest = OneTimeWorkRequestBuilder<SnoozeWorker>()
            .setInitialDelay(minutes.toLong(), TimeUnit.MINUTES)
            .setInputData(workDataOf("notification_id" to notificationId))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    /**
     * Reschedules all notifications after device boot.
     */
    fun rescheduleAllNotifications(context: Context) {
        // Re-enqueue periodic workers
        // The actual scheduling should be based on user preferences
        // This is called from BootReceiver
    }

    /**
     * Calculates delay in milliseconds until the specified time.
     */
    private fun calculateDelayUntilTime(targetHour: Int, targetMinute: Int): Long {
        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, targetHour)
            set(java.util.Calendar.MINUTE, targetMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        // If target time has passed today, schedule for tomorrow
        if (target.before(now) || target == now) {
            target.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}

/**
 * Worker for daily reminder notifications.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        NotificationHelper.showDailyReminderNotification(applicationContext)
        return Result.success()
    }
}

/**
 * Worker for streak reminder notifications.
 * Only shows notification if user hasn't been active today and has a streak.
 */
class StreakReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val app = applicationContext.applicationContext as? ProdiApplication
            if (app == null) {
                Timber.e("StreakReminderWorker: Could not get ProdiApplication")
                return Result.failure()
            }

            runBlocking {
                val userStats = app.userProgressRepository.getUserStats()
                val currentStreak = userStats?.currentStreak ?: 0

                // Only remind if user has an existing streak to protect
                if (currentStreak > 0) {
                    val todayActivity = app.userProgressRepository.getTodayActivity()
                    val hasActivityToday = todayActivity.wordsLearned > 0 ||
                            todayActivity.journalEntries > 0 ||
                            todayActivity.buddhaMessages > 0 ||
                            todayActivity.futureLettersWritten > 0 ||
                            todayActivity.totalActiveTimeSeconds > 60 // At least 1 minute active

                    if (!hasActivityToday) {
                        Timber.d("Showing streak reminder - user has $currentStreak day streak but no activity today")
                        NotificationHelper.showStreakReminderNotification(
                            applicationContext,
                            currentStreak
                        )
                    } else {
                        Timber.d("Skipping streak reminder - user was active today")
                    }
                } else {
                    Timber.d("Skipping streak reminder - user has no active streak")
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "StreakReminderWorker failed")
            Result.failure()
        }
    }
}

/**
 * Worker for wisdom/quote notifications.
 */
class WisdomNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        NotificationHelper.showWisdomNotification(applicationContext)
        return Result.success()
    }
}

/**
 * Worker for snoozed notifications.
 */
class SnoozeWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val notificationId = inputData.getInt("notification_id", 0)
        // Re-show the notification after snooze period
        NotificationHelper.showSnoozeEndedNotification(applicationContext, notificationId)
        return Result.success()
    }
}
