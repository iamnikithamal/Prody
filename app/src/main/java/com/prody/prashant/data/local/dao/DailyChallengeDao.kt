package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.ChallengeType
import com.prody.prashant.data.local.entity.DailyChallengeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Daily Challenges.
 */
@Dao
interface DailyChallengeDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: DailyChallengeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<DailyChallengeEntity>): List<Long>

    // ==================== Query Operations ====================

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    fun observeChallengesForDate(date: Long): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges WHERE date = :date ORDER BY createdAt ASC")
    suspend fun getChallengesForDate(date: Long): List<DailyChallengeEntity>

    @Query("SELECT * FROM daily_challenges WHERE id = :id")
    suspend fun getById(id: Long): DailyChallengeEntity?

    @Query("SELECT * FROM daily_challenges WHERE id = :id")
    fun observeById(id: Long): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenges WHERE isCompleted = 0 AND date = :date")
    fun observeActiveChallenges(date: Long): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges WHERE isCompleted = 1 AND date = :date")
    fun observeCompletedChallenges(date: Long): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges WHERE isCompleted = 1 ORDER BY completedAt DESC LIMIT :limit")
    fun observeRecentCompletedChallenges(limit: Int = 20): Flow<List<DailyChallengeEntity>>

    @Query("SELECT * FROM daily_challenges ORDER BY date DESC, createdAt ASC LIMIT :limit")
    fun observeRecentChallenges(limit: Int = 50): Flow<List<DailyChallengeEntity>>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(challenge: DailyChallengeEntity)

    @Query("UPDATE daily_challenges SET progress = :progress WHERE id = :id")
    suspend fun updateProgress(id: Long, progress: Int)

    @Query("""
        UPDATE daily_challenges SET
            isCompleted = 1,
            progress = requirement,
            completedAt = :completedAt
        WHERE id = :id
    """)
    suspend fun markCompleted(id: Long, completedAt: Long = System.currentTimeMillis())

    @Query("""
        UPDATE daily_challenges SET
            progress = CASE
                WHEN progress + :incrementBy >= requirement THEN requirement
                ELSE progress + :incrementBy
            END,
            isCompleted = CASE
                WHEN progress + :incrementBy >= requirement THEN 1
                ELSE 0
            END,
            completedAt = CASE
                WHEN progress + :incrementBy >= requirement THEN :timestamp
                ELSE NULL
            END
        WHERE id = :id
    """)
    suspend fun incrementProgress(id: Long, incrementBy: Int, timestamp: Long = System.currentTimeMillis())

    // ==================== Statistics ====================

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1")
    fun observeTotalCompleted(): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getTotalCompleted(): Int

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE isCompleted = 1 AND date = :date")
    suspend fun getCompletedCountForDate(date: Long): Int

    @Query("SELECT COUNT(DISTINCT date) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getDaysWithCompletedChallenges(): Int

    @Query("SELECT SUM(xpReward) FROM daily_challenges WHERE isCompleted = 1")
    suspend fun getTotalXpFromChallenges(): Int?

    @Query("SELECT type, COUNT(*) as count FROM daily_challenges WHERE isCompleted = 1 GROUP BY type ORDER BY count DESC")
    suspend fun getCompletionsByType(): List<ChallengeTypeCount>

    @Query("SELECT COUNT(*) FROM daily_challenges WHERE date = :date")
    suspend fun getChallengeCountForDate(date: Long): Int

    // ==================== Delete Operations ====================

    @Query("DELETE FROM daily_challenges WHERE date < :beforeDate AND isCompleted = 0")
    suspend fun deleteOldIncompleteChallenges(beforeDate: Long): Int

    @Query("DELETE FROM daily_challenges WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM daily_challenges")
    suspend fun deleteAll()
}

data class ChallengeTypeCount(
    val type: ChallengeType,
    val count: Int
)
