package com.prody.prashant.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.prody.prashant.data.local.entity.BuddhaMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "prodi_preferences")

/**
 * Manages app preferences using DataStore.
 * Handles onboarding state, theme, notification settings, and AI configuration.
 */
class PreferencesManager(context: Context) {

    private val dataStore = context.dataStore

    // ==================== Preference Keys ====================

    private object Keys {
        // Onboarding
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val ONBOARDING_STEP = intPreferencesKey("onboarding_step")

        // Theme
        val THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")

        // Notifications
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val WISDOM_NOTIFICATIONS_ENABLED = booleanPreferencesKey("wisdom_notifications_enabled")
        val STREAK_REMINDERS_ENABLED = booleanPreferencesKey("streak_reminders_enabled")

        // AI Settings
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val BUDDHA_MODE = stringPreferencesKey("buddha_mode")
        val AI_RESPONSE_LENGTH = stringPreferencesKey("ai_response_length") // "concise", "balanced", "detailed"

        // Learning Preferences
        val DAILY_WORD_GOAL = intPreferencesKey("daily_word_goal")
        val DAILY_JOURNAL_MINUTES_GOAL = intPreferencesKey("daily_journal_minutes_goal")
        val PREFERRED_LEARNING_TIME = stringPreferencesKey("preferred_learning_time")
        val AUTO_PLAY_PRONUNCIATION = booleanPreferencesKey("auto_play_pronunciation")
        val SHOW_WORD_OF_DAY = booleanPreferencesKey("show_word_of_day")
        val REVIEW_REMINDER_ENABLED = booleanPreferencesKey("review_reminder_enabled")

        // Journal Preferences
        val DEFAULT_JOURNAL_MOOD = stringPreferencesKey("default_journal_mood")
        val AUTO_ANALYZE_JOURNAL = booleanPreferencesKey("auto_analyze_journal")
        val JOURNAL_PROMPTS_ENABLED = booleanPreferencesKey("journal_prompts_enabled")

        // Privacy & Data
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CRASH_REPORTS_ENABLED = booleanPreferencesKey("crash_reports_enabled")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")

        // App State
        val LAST_OPEN_TIME = longPreferencesKey("last_open_time")
        val TOTAL_APP_OPENS = intPreferencesKey("total_app_opens")
        val CURRENT_VERSION_CODE = intPreferencesKey("current_version_code")
        val HAS_SEEN_WHATS_NEW = booleanPreferencesKey("has_seen_whats_new")

        // Feature Flags
        val EXPERIMENTAL_FEATURES_ENABLED = booleanPreferencesKey("experimental_features_enabled")
    }

    // ==================== Onboarding ====================

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { it[Keys.HAS_COMPLETED_ONBOARDING] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.HAS_COMPLETED_ONBOARDING] = completed }
    }

    val onboardingStep: Flow<Int> = dataStore.data
        .map { it[Keys.ONBOARDING_STEP] ?: 0 }

    suspend fun setOnboardingStep(step: Int) {
        dataStore.edit { it[Keys.ONBOARDING_STEP] = step }
    }

    // ==================== Theme ====================

    val themeMode: Flow<String> = dataStore.data
        .map { it[Keys.THEME_MODE] ?: "system" }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    val useDynamicColors: Flow<Boolean> = dataStore.data
        .map { it[Keys.USE_DYNAMIC_COLORS] ?: true }

    suspend fun setUseDynamicColors(use: Boolean) {
        dataStore.edit { it[Keys.USE_DYNAMIC_COLORS] = use }
    }

    // ==================== Notifications ====================

    val notificationsEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    val dailyReminderEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.DAILY_REMINDER_ENABLED] ?: true }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DAILY_REMINDER_ENABLED] = enabled }
    }

    data class ReminderTime(val hour: Int, val minute: Int)

    val dailyReminderTime: Flow<ReminderTime> = dataStore.data
        .map {
            ReminderTime(
                hour = it[Keys.DAILY_REMINDER_HOUR] ?: 9,
                minute = it[Keys.DAILY_REMINDER_MINUTE] ?: 0
            )
        }

    suspend fun setDailyReminderTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[Keys.DAILY_REMINDER_HOUR] = hour
            it[Keys.DAILY_REMINDER_MINUTE] = minute
        }
    }

    val wisdomNotificationsEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.WISDOM_NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setWisdomNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.WISDOM_NOTIFICATIONS_ENABLED] = enabled }
    }

    val streakRemindersEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.STREAK_REMINDERS_ENABLED] ?: true }

    suspend fun setStreakRemindersEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.STREAK_REMINDERS_ENABLED] = enabled }
    }

    // ==================== AI Settings ====================

    val geminiApiKey: Flow<String?> = dataStore.data
        .map { it[Keys.GEMINI_API_KEY] }

    suspend fun setGeminiApiKey(apiKey: String?) {
        dataStore.edit {
            if (apiKey != null) {
                it[Keys.GEMINI_API_KEY] = apiKey
            } else {
                it.remove(Keys.GEMINI_API_KEY)
            }
        }
    }

    val geminiModel: Flow<String> = dataStore.data
        .map { it[Keys.GEMINI_MODEL] ?: "gemini-1.5-flash" }

    suspend fun setGeminiModel(model: String) {
        dataStore.edit { it[Keys.GEMINI_MODEL] = model }
    }

    val buddhaMode: Flow<BuddhaMode> = dataStore.data
        .map {
            val modeName = it[Keys.BUDDHA_MODE] ?: BuddhaMode.STOIC.name
            try {
                BuddhaMode.valueOf(modeName)
            } catch (e: IllegalArgumentException) {
                BuddhaMode.STOIC
            }
        }

    suspend fun setBuddhaMode(mode: BuddhaMode) {
        dataStore.edit { it[Keys.BUDDHA_MODE] = mode.name }
    }

    val aiResponseLength: Flow<String> = dataStore.data
        .map { it[Keys.AI_RESPONSE_LENGTH] ?: "balanced" }

    suspend fun setAiResponseLength(length: String) {
        dataStore.edit { it[Keys.AI_RESPONSE_LENGTH] = length }
    }

    // ==================== Learning Preferences ====================

    val dailyWordGoal: Flow<Int> = dataStore.data
        .map { it[Keys.DAILY_WORD_GOAL] ?: 5 }

    suspend fun setDailyWordGoal(goal: Int) {
        dataStore.edit { it[Keys.DAILY_WORD_GOAL] = goal }
    }

    val dailyJournalMinutesGoal: Flow<Int> = dataStore.data
        .map { it[Keys.DAILY_JOURNAL_MINUTES_GOAL] ?: 10 }

    suspend fun setDailyJournalMinutesGoal(minutes: Int) {
        dataStore.edit { it[Keys.DAILY_JOURNAL_MINUTES_GOAL] = minutes }
    }

    val preferredLearningTime: Flow<String?> = dataStore.data
        .map { it[Keys.PREFERRED_LEARNING_TIME] }

    suspend fun setPreferredLearningTime(time: String?) {
        dataStore.edit {
            if (time != null) {
                it[Keys.PREFERRED_LEARNING_TIME] = time
            } else {
                it.remove(Keys.PREFERRED_LEARNING_TIME)
            }
        }
    }

    val autoPlayPronunciation: Flow<Boolean> = dataStore.data
        .map { it[Keys.AUTO_PLAY_PRONUNCIATION] ?: false }

    suspend fun setAutoPlayPronunciation(autoPlay: Boolean) {
        dataStore.edit { it[Keys.AUTO_PLAY_PRONUNCIATION] = autoPlay }
    }

    val showWordOfDay: Flow<Boolean> = dataStore.data
        .map { it[Keys.SHOW_WORD_OF_DAY] ?: true }

    suspend fun setShowWordOfDay(show: Boolean) {
        dataStore.edit { it[Keys.SHOW_WORD_OF_DAY] = show }
    }

    val reviewReminderEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.REVIEW_REMINDER_ENABLED] ?: true }

    suspend fun setReviewReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REVIEW_REMINDER_ENABLED] = enabled }
    }

    // ==================== Journal Preferences ====================

    val autoAnalyzeJournal: Flow<Boolean> = dataStore.data
        .map { it[Keys.AUTO_ANALYZE_JOURNAL] ?: true }

    suspend fun setAutoAnalyzeJournal(autoAnalyze: Boolean) {
        dataStore.edit { it[Keys.AUTO_ANALYZE_JOURNAL] = autoAnalyze }
    }

    val journalPromptsEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.JOURNAL_PROMPTS_ENABLED] ?: true }

    suspend fun setJournalPromptsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.JOURNAL_PROMPTS_ENABLED] = enabled }
    }

    // ==================== Privacy & Data ====================

    val analyticsEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.ANALYTICS_ENABLED] ?: false }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.ANALYTICS_ENABLED] = enabled }
    }

    val lastBackupTime: Flow<Long?> = dataStore.data
        .map { it[Keys.LAST_BACKUP_TIME] }

    suspend fun setLastBackupTime(time: Long) {
        dataStore.edit { it[Keys.LAST_BACKUP_TIME] = time }
    }

    val autoBackupEnabled: Flow<Boolean> = dataStore.data
        .map { it[Keys.AUTO_BACKUP_ENABLED] ?: false }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_BACKUP_ENABLED] = enabled }
    }

    // ==================== App State ====================

    val lastOpenTime: Flow<Long?> = dataStore.data
        .map { it[Keys.LAST_OPEN_TIME] }

    suspend fun setLastOpenTime(time: Long) {
        dataStore.edit { it[Keys.LAST_OPEN_TIME] = time }
    }

    val totalAppOpens: Flow<Int> = dataStore.data
        .map { it[Keys.TOTAL_APP_OPENS] ?: 0 }

    suspend fun incrementAppOpens() {
        dataStore.edit {
            val current = it[Keys.TOTAL_APP_OPENS] ?: 0
            it[Keys.TOTAL_APP_OPENS] = current + 1
        }
    }

    val hasSeenWhatsNew: Flow<Boolean> = dataStore.data
        .map { it[Keys.HAS_SEEN_WHATS_NEW] ?: false }

    suspend fun setHasSeenWhatsNew(seen: Boolean) {
        dataStore.edit { it[Keys.HAS_SEEN_WHATS_NEW] = seen }
    }

    // ==================== Utility ====================

    /**
     * Clears all preferences (for logout or data reset).
     */
    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }

    /**
     * Gets all preferences as a map (for export).
     */
    fun getAllPreferences(): Flow<Map<String, Any?>> = dataStore.data
        .map { preferences ->
            preferences.asMap().mapKeys { it.key.name }
        }
}
