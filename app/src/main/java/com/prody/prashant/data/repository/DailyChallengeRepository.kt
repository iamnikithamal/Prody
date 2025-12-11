package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.ChallengeTypeCount
import com.prody.prashant.data.local.dao.DailyChallengeDao
import com.prody.prashant.data.local.entity.ChallengeType
import com.prody.prashant.data.local.entity.DailyChallengeEntity
import com.prody.prashant.data.local.entity.DailyChallengeGenerator
import com.prody.prashant.data.local.entity.XpSource
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository for managing daily challenges.
 * Handles generation, tracking, and completion of challenges.
 */
class DailyChallengeRepository(
    private val dailyChallengeDao: DailyChallengeDao,
    private val userProgressRepository: UserProgressRepository
) {

    // ==================== Observe Operations ====================

    fun observeTodaysChallenges(): Flow<List<DailyChallengeEntity>> {
        return dailyChallengeDao.observeChallengesForDate(getTodayStartMillis())
    }

    fun observeActiveChallenges(): Flow<List<DailyChallengeEntity>> {
        return dailyChallengeDao.observeActiveChallenges(getTodayStartMillis())
    }

    fun observeCompletedChallenges(): Flow<List<DailyChallengeEntity>> {
        return dailyChallengeDao.observeCompletedChallenges(getTodayStartMillis())
    }

    fun observeRecentCompletedChallenges(limit: Int = 20): Flow<List<DailyChallengeEntity>> {
        return dailyChallengeDao.observeRecentCompletedChallenges(limit)
    }

    fun observeTotalCompleted(): Flow<Int> = dailyChallengeDao.observeTotalCompleted()

    // ==================== Query Operations ====================

    suspend fun getTodaysChallenges(): List<DailyChallengeEntity> {
        return dailyChallengeDao.getChallengesForDate(getTodayStartMillis())
    }

    suspend fun getById(id: Long): DailyChallengeEntity? = dailyChallengeDao.getById(id)

    suspend fun getTotalCompleted(): Int = dailyChallengeDao.getTotalCompleted()

    suspend fun getTotalXpFromChallenges(): Int = dailyChallengeDao.getTotalXpFromChallenges() ?: 0

    suspend fun getCompletionsByType(): List<ChallengeTypeCount> = dailyChallengeDao.getCompletionsByType()

    // ==================== Challenge Generation ====================

    /**
     * Generates daily challenges if they don't exist for today.
     * Returns the list of challenges for today.
     */
    suspend fun ensureTodaysChallengesExist(): List<DailyChallengeEntity> {
        val today = getTodayStartMillis()
        val existingChallenges = dailyChallengeDao.getChallengesForDate(today)

        if (existingChallenges.isNotEmpty()) {
            return existingChallenges
        }

        // Generate new challenges based on user context
        val userStats = userProgressRepository.getUserStats()
        val newChallenges = DailyChallengeGenerator.generateDailyChallenges(
            date = today,
            currentStreak = userStats?.currentStreak ?: 0,
            wordsLearned = userStats?.totalWordsLearned ?: 0,
            journalCount = userStats?.totalJournalEntries ?: 0,
            buddhaConversations = userStats?.totalBuddhaConversations ?: 0,
            futureLetters = userStats?.totalFutureLetters ?: 0,
            hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        )

        dailyChallengeDao.insertAll(newChallenges)
        return newChallenges
    }

    // ==================== Progress Tracking ====================

    /**
     * Updates challenge progress based on activity type.
     * Automatically marks as complete when requirement is met.
     */
    suspend fun updateProgressForActivity(
        activityType: ActivityType,
        count: Int = 1
    ) {
        val todaysChallenges = getTodaysChallenges()

        for (challenge in todaysChallenges) {
            if (challenge.isCompleted) continue

            val shouldUpdate = when (activityType) {
                ActivityType.WORD_LEARNED -> challenge.type == ChallengeType.LEARN_WORDS
                ActivityType.WORD_REVIEWED -> challenge.type == ChallengeType.REVIEW_WORDS
                ActivityType.JOURNAL_WRITTEN -> challenge.type == ChallengeType.WRITE_JOURNAL
                ActivityType.LONG_JOURNAL -> challenge.type == ChallengeType.LONG_JOURNAL
                ActivityType.BUDDHA_MESSAGE -> challenge.type == ChallengeType.BUDDHA_CHAT ||
                        challenge.type == ChallengeType.DEEP_CONVERSATION
                ActivityType.FUTURE_LETTER -> challenge.type == ChallengeType.FUTURE_LETTER
                ActivityType.QUOTE_READ -> challenge.type == ChallengeType.QUOTE_REFLECTION
                ActivityType.ANY_ACTIVITY -> challenge.type == ChallengeType.STREAK_MAINTAIN ||
                        challenge.type == ChallengeType.MIXED_ACTIVITY ||
                        challenge.type == ChallengeType.EARLY_BIRD ||
                        challenge.type == ChallengeType.NIGHT_OWL
            }

            if (shouldUpdate) {
                incrementChallengeProgress(challenge.id, count)
            }
        }
    }

    /**
     * Increments progress and checks for completion.
     * Awards XP when completed.
     */
    suspend fun incrementChallengeProgress(challengeId: Long, increment: Int = 1) {
        val challenge = dailyChallengeDao.getById(challengeId) ?: return
        if (challenge.isCompleted) return

        val newProgress = (challenge.progress + increment).coerceAtMost(challenge.requirement)
        val wasCompleted = newProgress >= challenge.requirement

        if (wasCompleted) {
            dailyChallengeDao.markCompleted(challengeId)
            // Award XP for completing challenge
            userProgressRepository.awardXp(
                amount = challenge.xpReward,
                source = XpSource.CHALLENGE_COMPLETED,
                description = "Completed: ${challenge.title}"
            )
        } else {
            dailyChallengeDao.updateProgress(challengeId, newProgress)
        }
    }

    /**
     * Manually marks a challenge as complete.
     */
    suspend fun completeChallenge(challengeId: Long) {
        val challenge = dailyChallengeDao.getById(challengeId) ?: return
        if (challenge.isCompleted) return

        dailyChallengeDao.markCompleted(challengeId)
        userProgressRepository.awardXp(
            amount = challenge.xpReward,
            source = XpSource.CHALLENGE_COMPLETED,
            description = "Completed: ${challenge.title}"
        )
    }

    // ==================== Cleanup ====================

    /**
     * Removes old incomplete challenges (older than specified days).
     */
    suspend fun cleanupOldChallenges(olderThanDays: Int = 7) {
        val cutoffDate = getTodayStartMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        dailyChallengeDao.deleteOldIncompleteChallenges(cutoffDate)
    }

    // ==================== Utility ====================

    private fun getTodayStartMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

/**
 * Types of activities that can progress challenges.
 */
enum class ActivityType {
    WORD_LEARNED,
    WORD_REVIEWED,
    JOURNAL_WRITTEN,
    LONG_JOURNAL,
    BUDDHA_MESSAGE,
    FUTURE_LETTER,
    QUOTE_READ,
    ANY_ACTIVITY
}

/**
 * Result of completing a challenge.
 */
data class ChallengeCompletionResult(
    val challenge: DailyChallengeEntity,
    val xpAwarded: Int,
    val newLevel: Int?,
    val badgeUnlocked: String?
)
