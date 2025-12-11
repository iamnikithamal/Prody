package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    // ==================== Daily Activity ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyActivity(activity: DailyActivityEntity): Long

    @Update
    suspend fun updateDailyActivity(activity: DailyActivityEntity)

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    suspend fun getDailyActivityByDate(date: Long): DailyActivityEntity?

    @Query("SELECT * FROM daily_activity WHERE date = :date")
    fun observeDailyActivityByDate(date: Long): Flow<DailyActivityEntity?>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC")
    fun observeAllDailyActivities(): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun observeDailyActivitiesInRange(startDate: Long, endDate: Long): Flow<List<DailyActivityEntity>>

    @Query("SELECT * FROM daily_activity WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getDailyActivitiesInRange(startDate: Long, endDate: Long): List<DailyActivityEntity>

    @Query("SELECT * FROM daily_activity ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentDailyActivities(limit: Int = 30): List<DailyActivityEntity>

    @Query("""
        UPDATE daily_activity SET
            wordsLearned = wordsLearned + :wordsLearned,
            wordsReviewed = wordsReviewed + :wordsReviewed,
            journalEntries = journalEntries + :journalEntries,
            journalWords = journalWords + :journalWords,
            buddhaMessages = buddhaMessages + :buddhaMessages,
            futureLettersWritten = futureLettersWritten + :futureLettersWritten,
            futureLettersOpened = futureLettersOpened + :futureLettersOpened,
            xpEarned = xpEarned + :xpEarned,
            lastSessionAt = :timestamp,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun incrementDailyActivity(
        date: Long,
        wordsLearned: Int = 0,
        wordsReviewed: Int = 0,
        journalEntries: Int = 0,
        journalWords: Int = 0,
        buddhaMessages: Int = 0,
        futureLettersWritten: Int = 0,
        futureLettersOpened: Int = 0,
        xpEarned: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE daily_activity SET
            learningTimeSeconds = learningTimeSeconds + :learningTime,
            journalingTimeSeconds = journalingTimeSeconds + :journalingTime,
            buddhaTimeSeconds = buddhaTimeSeconds + :buddhaTime,
            totalActiveTimeSeconds = totalActiveTimeSeconds + :totalTime,
            updatedAt = :timestamp
        WHERE date = :date
    """)
    suspend fun addActivityTime(
        date: Long,
        learningTime: Int = 0,
        journalingTime: Int = 0,
        buddhaTime: Int = 0,
        totalTime: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== User Stats ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStatsEntity): Long

    @Update
    suspend fun updateUserStats(stats: UserStatsEntity)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun observeUserStats(): Flow<UserStatsEntity?>

    @Query("""
        UPDATE user_stats SET
            currentXp = currentXp + :xp,
            totalXp = totalXp + :xp,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun addXp(xp: Int, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            level = :level,
            levelTitle = :levelTitle,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateLevel(level: Int, levelTitle: LevelTitle, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            currentStreak = :currentStreak,
            longestStreak = CASE WHEN :currentStreak > longestStreak THEN :currentStreak ELSE longestStreak END,
            lastActiveDate = :lastActiveDate,
            streakStartDate = CASE WHEN :isNewStreak = 1 THEN :lastActiveDate ELSE streakStartDate END,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateStreak(
        currentStreak: Int,
        lastActiveDate: Long,
        isNewStreak: Boolean = false,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_stats SET
            totalWordsLearned = totalWordsLearned + :learned,
            totalWordsMastered = totalWordsMastered + :mastered,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementWordStats(learned: Int = 0, mastered: Int = 0, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            totalJournalEntries = totalJournalEntries + :entries,
            totalJournalWords = totalJournalWords + :words,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementJournalStats(entries: Int = 0, words: Int = 0, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            totalBuddhaConversations = totalBuddhaConversations + :conversations,
            totalBuddhaMessages = totalBuddhaMessages + :messages,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementBuddhaStats(conversations: Int = 0, messages: Int = 0, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            totalFutureLetters = totalFutureLetters + :written,
            totalFutureLettersOpened = totalFutureLettersOpened + :opened,
            totalCommitmentsKept = totalCommitmentsKept + :commitmentsKept,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun incrementFutureStats(written: Int = 0, opened: Int = 0, commitmentsKept: Int = 0, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            totalLearningTime = totalLearningTime + :learningTime,
            totalJournalingTime = totalJournalingTime + :journalingTime,
            totalBuddhaTime = totalBuddhaTime + :buddhaTime,
            totalActiveTime = totalActiveTime + :totalTime,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun addTotalTime(
        learningTime: Int = 0,
        journalingTime: Int = 0,
        buddhaTime: Int = 0,
        totalTime: Int = 0,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE user_stats SET
            displayName = :name,
            bio = :bio,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateProfile(name: String, bio: String?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_stats SET avatarId = :avatarId, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateAvatar(avatarId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_stats SET bannerId = :bannerId, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateBanner(bannerId: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            badgesEarned = :badges,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateBadges(badges: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            unlockedAvatars = :avatars,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateUnlockedAvatars(avatars: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            unlockedBanners = :banners,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updateUnlockedBanners(banners: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE user_stats SET
            preferredLearningTime = :time,
            dailyGoalWords = :words,
            dailyGoalJournalMinutes = :journalMinutes,
            focusAreas = :focusAreas,
            updatedAt = :timestamp
        WHERE id = 1
    """)
    suspend fun updatePreferences(
        time: String?,
        words: Int,
        journalMinutes: Int,
        focusAreas: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== Badges ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>): List<Long>

    @Update
    suspend fun updateBadge(badge: BadgeEntity)

    @Query("SELECT * FROM badges WHERE badgeId = :badgeId")
    suspend fun getBadgeById(badgeId: String): BadgeEntity?

    @Query("SELECT * FROM badges WHERE badgeId = :badgeId")
    fun observeBadgeById(badgeId: String): Flow<BadgeEntity?>

    @Query("SELECT * FROM badges ORDER BY isEarned DESC, earnedAt DESC")
    fun observeAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE isEarned = 1 ORDER BY earnedAt DESC")
    fun observeEarnedBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE isEarned = 0 ORDER BY progress DESC")
    fun observeUnearnedBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badges WHERE category = :category ORDER BY isEarned DESC, tier ASC")
    fun observeBadgesByCategory(category: BadgeCategory): Flow<List<BadgeEntity>>

    @Query("""
        UPDATE badges SET
            isEarned = 1,
            earnedAt = :earnedAt,
            progress = requirement
        WHERE badgeId = :badgeId
    """)
    suspend fun markBadgeEarned(badgeId: String, earnedAt: Long = System.currentTimeMillis())

    @Query("UPDATE badges SET progress = :progress WHERE badgeId = :badgeId")
    suspend fun updateBadgeProgress(badgeId: String, progress: Int)

    @Query("SELECT COUNT(*) FROM badges WHERE isEarned = 1")
    fun observeEarnedBadgeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM badges WHERE isEarned = 1")
    suspend fun getEarnedBadgeCount(): Int

    // ==================== XP Transactions ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXpTransaction(transaction: XpTransactionEntity): Long

    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC")
    fun observeAllXpTransactions(): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentXpTransactions(limit: Int = 50): Flow<List<XpTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE timestamp >= :since ORDER BY timestamp DESC")
    suspend fun getXpTransactionsSince(since: Long): List<XpTransactionEntity>

    @Query("SELECT SUM(amount) FROM xp_transactions WHERE timestamp >= :since")
    suspend fun getTotalXpSince(since: Long): Int?

    @Query("SELECT SUM(amount) FROM xp_transactions WHERE source = :source AND timestamp >= :since")
    suspend fun getXpBySourceSince(source: XpSource, since: Long): Int?

    @Query("SELECT source, SUM(amount) as total FROM xp_transactions GROUP BY source ORDER BY total DESC")
    suspend fun getXpBySource(): List<XpBySource>

    // ==================== Activity Calendar ====================

    @Query("""
        SELECT date, totalActiveTimeSeconds > 0 as wasActive
        FROM daily_activity
        WHERE date >= :startDate AND date <= :endDate
        ORDER BY date ASC
    """)
    suspend fun getActivityCalendar(startDate: Long, endDate: Long): List<ActivityCalendarDay>

    @Query("SELECT COUNT(DISTINCT date) FROM daily_activity WHERE totalActiveTimeSeconds > 0")
    suspend fun getTotalActiveDays(): Int

    @Query("SELECT COUNT(DISTINCT date) FROM daily_activity WHERE totalActiveTimeSeconds > 0")
    fun observeTotalActiveDays(): Flow<Int>
}

data class XpBySource(
    val source: XpSource,
    val total: Int
)

data class ActivityCalendarDay(
    val date: Long,
    val wasActive: Boolean
)
