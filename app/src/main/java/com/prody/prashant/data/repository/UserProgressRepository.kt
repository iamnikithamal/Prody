package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.ActivityCalendarDay
import com.prody.prashant.data.local.dao.UserProgressDao
import com.prody.prashant.data.local.dao.XpBySource
import com.prody.prashant.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Repository for user progress, statistics, and gamification.
 * Handles XP, levels, streaks, badges, and activity tracking.
 */
class UserProgressRepository(
    private val userProgressDao: UserProgressDao
) {

    // ==================== User Stats Observe Operations ====================

    fun observeUserStats(): Flow<UserStatsEntity?> = userProgressDao.observeUserStats()

    fun observeTotalActiveDays(): Flow<Int> = userProgressDao.observeTotalActiveDays()

    // ==================== User Stats Operations ====================

    suspend fun getUserStats(): UserStatsEntity? = userProgressDao.getUserStats()

    suspend fun initializeUserStats(): UserStatsEntity {
        val existing = getUserStats()
        if (existing != null) return existing

        val newStats = UserStatsEntity()
        userProgressDao.insertUserStats(newStats)
        return newStats
    }

    suspend fun updateProfile(name: String, bio: String?) =
        userProgressDao.updateProfile(name, bio)

    suspend fun updateAvatar(avatarId: String) =
        userProgressDao.updateAvatar(avatarId)

    suspend fun updateBanner(bannerId: String) =
        userProgressDao.updateBanner(bannerId)

    suspend fun updatePreferences(
        preferredLearningTime: String?,
        dailyGoalWords: Int,
        dailyGoalJournalMinutes: Int,
        focusAreas: String?
    ) = userProgressDao.updatePreferences(
        preferredLearningTime,
        dailyGoalWords,
        dailyGoalJournalMinutes,
        focusAreas
    )

    // ==================== XP and Level Operations ====================

    /**
     * Awards XP to the user and handles level-up logic.
     * @return New total XP and whether user leveled up
     */
    suspend fun awardXp(amount: Int, source: XpSource, description: String, relatedEntityId: Long? = null): XpAwardResult {
        // Record transaction
        val transaction = XpTransactionEntity(
            amount = amount,
            source = source,
            description = description,
            relatedEntityId = relatedEntityId
        )
        userProgressDao.insertXpTransaction(transaction)

        // Update user stats
        userProgressDao.addXp(amount)

        // Check for level up
        val stats = getUserStats() ?: return XpAwardResult(amount, false, 1)
        val newTotalXp = stats.totalXp + amount
        val newLevelTitle = LevelTitle.fromXp(newTotalXp)
        val leveledUp = newLevelTitle != stats.levelTitle

        if (leveledUp) {
            val newLevel = calculateLevel(newTotalXp)
            userProgressDao.updateLevel(newLevel, newLevelTitle)
        }

        return XpAwardResult(
            xpAwarded = amount,
            leveledUp = leveledUp,
            newLevel = if (leveledUp) calculateLevel(newTotalXp) else stats.level
        )
    }

    private fun calculateLevel(totalXp: Int): Int {
        // Level 1: 0-499, Level 2: 500-999, Level 3: 1000-1999, etc.
        return when {
            totalXp < 500 -> 1
            totalXp < 1000 -> 2
            totalXp < 2000 -> 3
            totalXp < 5000 -> 4 + (totalXp - 2000) / 1000
            totalXp < 10000 -> 7 + (totalXp - 5000) / 1500
            else -> 10 + (totalXp - 10000) / 2000
        }
    }

    fun observeRecentXpTransactions(limit: Int = 50): Flow<List<XpTransactionEntity>> =
        userProgressDao.observeRecentXpTransactions(limit)

    suspend fun getXpTransactionsSince(since: Long): List<XpTransactionEntity> =
        userProgressDao.getXpTransactionsSince(since)

    suspend fun getTotalXpSince(since: Long): Int =
        userProgressDao.getTotalXpSince(since) ?: 0

    suspend fun getXpBySource(): List<XpBySource> = userProgressDao.getXpBySource()

    // ==================== Streak Operations ====================

    /**
     * Records user activity for today and updates streak.
     */
    suspend fun recordDailyActivity() {
        val today = getTodayStartMillis()
        val stats = getUserStats() ?: initializeUserStats()

        val lastActiveNormalized = stats.lastActiveDate?.let { normalizeToStartOfDay(it) }
        val yesterday = getYesterdayStartMillis()

        val (newStreak, isNewStreak) = when {
            // Already active today
            lastActiveNormalized == today -> Pair(stats.currentStreak, false)
            // Was active yesterday - continue streak
            lastActiveNormalized == yesterday -> Pair(stats.currentStreak + 1, false)
            // Streak broken or first activity
            else -> Pair(1, true)
        }

        userProgressDao.updateStreak(newStreak, today, isNewStreak)

        // Award streak bonus XP
        if (newStreak > stats.currentStreak) {
            val bonusXp = XpSource.STREAK_BONUS.baseXp * newStreak
            awardXp(bonusXp, XpSource.STREAK_BONUS, "Day $newStreak streak bonus")
        }

        // Check streak badges
        checkStreakBadges(newStreak)
    }

    private suspend fun checkStreakBadges(streak: Int) {
        when {
            streak >= 7 -> awardBadgeIfNotEarned("week_streak")
            streak >= 30 -> awardBadgeIfNotEarned("month_streak")
            streak >= 90 -> awardBadgeIfNotEarned("quarter_streak")
        }
    }

    // ==================== Daily Activity Operations ====================

    fun observeDailyActivityByDate(date: Long): Flow<DailyActivityEntity?> =
        userProgressDao.observeDailyActivityByDate(date)

    fun observeDailyActivitiesInRange(startDate: Long, endDate: Long): Flow<List<DailyActivityEntity>> =
        userProgressDao.observeDailyActivitiesInRange(startDate, endDate)

    suspend fun getTodayActivity(): DailyActivityEntity {
        val today = getTodayStartMillis()
        return userProgressDao.getDailyActivityByDate(today)
            ?: DailyActivityEntity(date = today).also {
                userProgressDao.insertDailyActivity(it)
            }
    }

    suspend fun incrementTodayStats(
        wordsLearned: Int = 0,
        wordsReviewed: Int = 0,
        journalEntries: Int = 0,
        journalWords: Int = 0,
        buddhaMessages: Int = 0,
        futureLettersWritten: Int = 0,
        futureLettersOpened: Int = 0,
        xpEarned: Int = 0
    ) {
        val today = getTodayStartMillis()
        getTodayActivity() // Ensure today's record exists
        userProgressDao.incrementDailyActivity(
            today, wordsLearned, wordsReviewed, journalEntries, journalWords,
            buddhaMessages, futureLettersWritten, futureLettersOpened, xpEarned
        )

        // Also update lifetime stats
        if (wordsLearned > 0) userProgressDao.incrementWordStats(learned = wordsLearned)
        if (journalEntries > 0) userProgressDao.incrementJournalStats(entries = journalEntries, words = journalWords)
        if (buddhaMessages > 0) userProgressDao.incrementBuddhaStats(messages = buddhaMessages)
        if (futureLettersWritten > 0) userProgressDao.incrementFutureStats(written = futureLettersWritten)
        if (futureLettersOpened > 0) userProgressDao.incrementFutureStats(opened = futureLettersOpened)
    }

    suspend fun addTodayActiveTime(
        learningTime: Int = 0,
        journalingTime: Int = 0,
        buddhaTime: Int = 0,
        totalTime: Int = 0
    ) {
        val today = getTodayStartMillis()
        getTodayActivity() // Ensure today's record exists
        userProgressDao.addActivityTime(today, learningTime, journalingTime, buddhaTime, totalTime)
        userProgressDao.addTotalTime(learningTime, journalingTime, buddhaTime, totalTime)
    }

    suspend fun getRecentDailyActivities(limit: Int = 30): List<DailyActivityEntity> =
        userProgressDao.getRecentDailyActivities(limit)

    suspend fun getActivityCalendar(startDate: Long, endDate: Long): List<ActivityCalendarDay> =
        userProgressDao.getActivityCalendar(startDate, endDate)

    // ==================== Badge Operations ====================

    fun observeAllBadges(): Flow<List<BadgeEntity>> = userProgressDao.observeAllBadges()

    fun observeEarnedBadges(): Flow<List<BadgeEntity>> = userProgressDao.observeEarnedBadges()

    fun observeUnearnedBadges(): Flow<List<BadgeEntity>> = userProgressDao.observeUnearnedBadges()

    fun observeBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>> =
        userProgressDao.observeBadgesByCategory(category)

    fun observeEarnedBadgeCount(): Flow<Int> = userProgressDao.observeEarnedBadgeCount()

    suspend fun getBadgeById(badgeId: String): BadgeEntity? =
        userProgressDao.getBadgeById(badgeId)

    suspend fun getEarnedBadgeCount(): Int = userProgressDao.getEarnedBadgeCount()

    /**
     * Awards a badge to the user if not already earned.
     * @return true if badge was newly awarded, false if already earned
     */
    suspend fun awardBadge(badgeId: String): Boolean {
        val badge = userProgressDao.getBadgeById(badgeId) ?: return false
        if (badge.isEarned) return false

        userProgressDao.markBadgeEarned(badgeId)

        // Award XP for badge
        if (badge.xpReward > 0) {
            awardXp(badge.xpReward, XpSource.BADGE_EARNED, "Earned badge: ${badge.name}")
        }

        // Unlock avatar/banner if applicable
        badge.unlocksAvatar?.let { avatarId ->
            val stats = getUserStats() ?: return@let
            val currentAvatars = stats.unlockedAvatars.split(",").toMutableSet()
            currentAvatars.add(avatarId)
            userProgressDao.updateUnlockedAvatars(currentAvatars.joinToString(","))
        }

        badge.unlocksBanner?.let { bannerId ->
            val stats = getUserStats() ?: return@let
            val currentBanners = stats.unlockedBanners.split(",").toMutableSet()
            currentBanners.add(bannerId)
            userProgressDao.updateUnlockedBanners(currentBanners.joinToString(","))
        }

        // Update user's earned badges list
        val stats = getUserStats() ?: return true
        val currentBadges = stats.badgesEarned.split(",").filter { it.isNotBlank() }.toMutableSet()
        currentBadges.add(badgeId)
        userProgressDao.updateBadges(currentBadges.joinToString(","))

        return true
    }

    private suspend fun awardBadgeIfNotEarned(badgeId: String) {
        val badge = userProgressDao.getBadgeById(badgeId)
        if (badge != null && !badge.isEarned) {
            awardBadge(badgeId)
        }
    }

    suspend fun updateBadgeProgress(badgeId: String, progress: Int) {
        userProgressDao.updateBadgeProgress(badgeId, progress)
        val badge = userProgressDao.getBadgeById(badgeId) ?: return
        if (progress >= badge.requirement && !badge.isEarned) {
            awardBadge(badgeId)
        }
    }

    // ==================== Statistics Increment Helpers ====================

    suspend fun incrementWordStats(learned: Int = 0, mastered: Int = 0) {
        userProgressDao.incrementWordStats(learned, mastered)
        incrementTodayStats(wordsLearned = learned)

        // Check learning badges
        val stats = getUserStats() ?: return
        val totalLearned = stats.totalWordsLearned + learned
        when {
            totalLearned >= 1 -> updateBadgeProgress("first_word", 1)
            totalLearned >= 10 -> updateBadgeProgress("ten_words", 10)
            totalLearned >= 50 -> updateBadgeProgress("fifty_words", 50)
            totalLearned >= 100 -> updateBadgeProgress("hundred_words", 100)
        }
    }

    suspend fun incrementBuddhaConversations() {
        userProgressDao.incrementBuddhaStats(conversations = 1)

        // Check Buddha badges
        val stats = getUserStats() ?: return
        val totalConversations = stats.totalBuddhaConversations + 1
        when {
            totalConversations >= 1 -> updateBadgeProgress("first_buddha", 1)
            totalConversations >= 10 -> updateBadgeProgress("ten_buddha", 10)
        }
    }

    // ==================== Time of Day Badges ====================

    suspend fun checkTimeOfDayBadges() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 6 -> awardBadgeIfNotEarned("early_bird")
            hour >= 0 && hour < 4 -> awardBadgeIfNotEarned("night_owl")
        }
    }

    // ==================== Utility Functions ====================

    private fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun getYesterdayStartMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    private fun normalizeToStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

data class XpAwardResult(
    val xpAwarded: Int,
    val leveledUp: Boolean,
    val newLevel: Int
)
