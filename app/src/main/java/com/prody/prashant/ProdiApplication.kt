package com.prody.prashant

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import com.prody.prashant.ai.BuddhaAiService
import com.prody.prashant.data.local.PreferencesManager
import com.prody.prashant.data.local.ProdiDatabase
import com.prody.prashant.data.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Application class for Prodi.
 * Initializes database, repositories, and notification channels.
 */
class ProdiApplication : Application(), Configuration.Provider {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Lazy initialization of database
    val database: ProdiDatabase by lazy { ProdiDatabase.getInstance(this) }

    // Lazy initialization of preferences
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }

    // Lazy initialization of repositories
    val vocabularyRepository: VocabularyRepository by lazy {
        VocabularyRepository(database.vocabularyDao())
    }

    val journalRepository: JournalRepository by lazy {
        JournalRepository(database.journalDao())
    }

    val futureSelfRepository: FutureSelfRepository by lazy {
        FutureSelfRepository(database.futureSelfDao())
    }

    val buddhaRepository: BuddhaRepository by lazy {
        BuddhaRepository(database.buddhaDao())
    }

    val userProgressRepository: UserProgressRepository by lazy {
        UserProgressRepository(database.userProgressDao())
    }

    val dailyChallengeRepository: DailyChallengeRepository by lazy {
        DailyChallengeRepository(database.dailyChallengeDao(), userProgressRepository)
    }

    // AI Service - initialized lazily with API key from preferences
    var buddhaAiService: BuddhaAiService? = null
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }

        Timber.d("Prodi Application starting...")

        // Create notification channels
        createNotificationChannels()

        // Initialize AI service with stored API key
        initializeAiService()

        // Record app open
        applicationScope.launch {
            preferencesManager.incrementAppOpens()
            preferencesManager.setLastOpenTime(System.currentTimeMillis())

            // Initialize user stats if needed
            userProgressRepository.initializeUserStats()

            // Record daily activity
            userProgressRepository.recordDailyActivity()

            // Check for delivered future self letters
            futureSelfRepository.processDeliveries()
        }
    }

    private fun initializeAiService() {
        applicationScope.launch {
            val apiKey = preferencesManager.geminiApiKey.first()
            val model = preferencesManager.geminiModel.first()
            buddhaAiService = BuddhaAiService(apiKey, model)

            // Listen for API key changes
            preferencesManager.geminiApiKey.collect { newKey ->
                buddhaAiService?.updateApiKey(newKey)
            }
        }

        applicationScope.launch {
            preferencesManager.geminiModel.collect { newModel ->
                buddhaAiService?.updateModel(newModel)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Daily Inspiration Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_INSPIRATION,
                getString(R.string.notification_channel_daily),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily wisdom and inspiration notifications"
                enableLights(true)
                enableVibration(true)
            }

            // Streak Reminders Channel
            val streakChannel = NotificationChannel(
                CHANNEL_STREAK_REMINDER,
                getString(R.string.notification_channel_streak),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to maintain your streak"
                enableLights(true)
                enableVibration(true)
            }

            // Future Self Letters Channel
            val futureChannel = NotificationChannel(
                CHANNEL_FUTURE_SELF,
                getString(R.string.notification_channel_future),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications when letters from your past self are delivered"
                enableLights(true)
                enableVibration(true)
            }

            // Daily Wisdom Channel
            val wisdomChannel = NotificationChannel(
                CHANNEL_WISDOM,
                getString(R.string.notification_channel_wisdom),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily wisdom quotes and words"
            }

            notificationManager.createNotificationChannels(
                listOf(dailyChannel, streakChannel, futureChannel, wisdomChannel)
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    companion object {
        lateinit var instance: ProdiApplication
            private set

        const val CHANNEL_DAILY_INSPIRATION = "daily_inspiration"
        const val CHANNEL_STREAK_REMINDER = "streak_reminder"
        const val CHANNEL_FUTURE_SELF = "future_self"
        const val CHANNEL_WISDOM = "wisdom"
    }

    /**
     * Release tree that filters out debug and verbose logs,
     * and reports errors/warnings to crash reporting service.
     */
    private class ReleaseTree : Timber.Tree() {
        override fun isLoggable(tag: String?, priority: Int): Boolean {
            return priority >= android.util.Log.INFO
        }

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == android.util.Log.ERROR || priority == android.util.Log.WARN) {
                // In production, you could send errors to a crash reporting service
                // For now, just log to console
                when (priority) {
                    android.util.Log.ERROR -> android.util.Log.e(tag, message, t)
                    android.util.Log.WARN -> android.util.Log.w(tag, message, t)
                    else -> android.util.Log.i(tag, message)
                }
            }
        }
    }
}
