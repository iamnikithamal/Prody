package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.CommitmentEntity
import com.prody.prashant.data.local.entity.CommitmentStatus
import com.prody.prashant.data.local.entity.FutureSelfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FutureSelfDao {

    // ==================== Future Self Letters ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: FutureSelfEntity): Long

    @Update
    suspend fun updateLetter(letter: FutureSelfEntity)

    @Delete
    suspend fun deleteLetter(letter: FutureSelfEntity)

    @Query("DELETE FROM future_self_letters WHERE id = :id")
    suspend fun deleteLetterById(id: Long)

    @Query("SELECT * FROM future_self_letters WHERE id = :id")
    suspend fun getLetterById(id: Long): FutureSelfEntity?

    @Query("SELECT * FROM future_self_letters WHERE id = :id")
    fun observeLetterById(id: Long): Flow<FutureSelfEntity?>

    @Query("SELECT * FROM future_self_letters ORDER BY createdAt DESC")
    fun observeAllLetters(): Flow<List<FutureSelfEntity>>

    @Query("SELECT * FROM future_self_letters WHERE isDelivered = 0 ORDER BY deliveryDate ASC")
    fun observePendingLetters(): Flow<List<FutureSelfEntity>>

    @Query("SELECT * FROM future_self_letters WHERE isDelivered = 1 ORDER BY deliveryDate DESC")
    fun observeDeliveredLetters(): Flow<List<FutureSelfEntity>>

    @Query("SELECT * FROM future_self_letters WHERE isDelivered = 1 AND isOpened = 0 ORDER BY deliveryDate ASC")
    fun observeUnreadDeliveredLetters(): Flow<List<FutureSelfEntity>>

    @Query("SELECT * FROM future_self_letters WHERE isOpened = 1 ORDER BY openedAt DESC")
    fun observeOpenedLetters(): Flow<List<FutureSelfEntity>>

    // ==================== Delivery Management ====================

    @Query("""
        UPDATE future_self_letters
        SET isDelivered = 1, updatedAt = :timestamp
        WHERE deliveryDate <= :currentTime AND isDelivered = 0
    """)
    suspend fun markDueLettersAsDelivered(currentTime: Long = System.currentTimeMillis(), timestamp: Long = System.currentTimeMillis()): Int

    @Query("""
        SELECT * FROM future_self_letters
        WHERE deliveryDate <= :currentTime AND isDelivered = 0
    """)
    suspend fun getLettersDueForDelivery(currentTime: Long = System.currentTimeMillis()): List<FutureSelfEntity>

    @Query("""
        UPDATE future_self_letters
        SET isOpened = 1, openedAt = :openedAt, updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun markLetterAsOpened(id: Long, openedAt: Long = System.currentTimeMillis(), timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE future_self_letters
        SET reflectionAfterOpening = :reflection,
            goalsAchieved = :goalsAchieved,
            achievementScore = :score,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateLetterReflection(
        id: Long,
        reflection: String?,
        goalsAchieved: String?,
        score: Int?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE future_self_letters
        SET aiAnalysis = :analysis, aiEncouragement = :encouragement, updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateAiAnalysis(id: Long, analysis: String?, encouragement: String?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE future_self_letters SET notificationScheduled = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun markNotificationScheduled(id: Long, timestamp: Long = System.currentTimeMillis())

    // ==================== Statistics ====================

    @Query("SELECT COUNT(*) FROM future_self_letters")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM future_self_letters")
    suspend fun getTotalLetterCount(): Int

    @Query("SELECT COUNT(*) FROM future_self_letters WHERE isDelivered = 0")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM future_self_letters WHERE isDelivered = 1 AND isOpened = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM future_self_letters WHERE isOpened = 1")
    fun observeOpenedCount(): Flow<Int>

    @Query("SELECT AVG(achievementScore) FROM future_self_letters WHERE achievementScore IS NOT NULL")
    suspend fun getAverageAchievementScore(): Float?

    @Query("SELECT COUNT(*) FROM future_self_letters WHERE createdAt >= :since")
    suspend fun getCountSince(since: Long): Int

    // ==================== Upcoming Letters ====================

    @Query("""
        SELECT * FROM future_self_letters
        WHERE isDelivered = 0
        ORDER BY deliveryDate ASC
        LIMIT 1
    """)
    suspend fun getNextUpcomingLetter(): FutureSelfEntity?

    @Query("""
        SELECT * FROM future_self_letters
        WHERE isDelivered = 0
        ORDER BY deliveryDate ASC
        LIMIT 1
    """)
    fun observeNextUpcomingLetter(): Flow<FutureSelfEntity?>

    @Query("""
        SELECT * FROM future_self_letters
        WHERE deliveryDate BETWEEN :startTime AND :endTime
        AND isDelivered = 0
        ORDER BY deliveryDate ASC
    """)
    suspend fun getLettersDeliveryInRange(startTime: Long, endTime: Long): List<FutureSelfEntity>

    // ==================== Commitments ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: CommitmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitments(commitments: List<CommitmentEntity>): List<Long>

    @Update
    suspend fun updateCommitment(commitment: CommitmentEntity)

    @Delete
    suspend fun deleteCommitment(commitment: CommitmentEntity)

    @Query("SELECT * FROM commitments WHERE id = :id")
    suspend fun getCommitmentById(id: Long): CommitmentEntity?

    @Query("SELECT * FROM commitments WHERE letterId = :letterId ORDER BY createdAt ASC")
    fun observeCommitmentsByLetter(letterId: Long): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM commitments WHERE letterId = :letterId")
    suspend fun getCommitmentsByLetter(letterId: Long): List<CommitmentEntity>

    @Query("SELECT * FROM commitments WHERE status = :status ORDER BY targetDate ASC")
    fun observeCommitmentsByStatus(status: CommitmentStatus): Flow<List<CommitmentEntity>>

    @Query("""
        UPDATE commitments
        SET status = :status, completedAt = :completedAt, notes = :notes
        WHERE id = :id
    """)
    suspend fun updateCommitmentStatus(
        id: Long,
        status: CommitmentStatus,
        completedAt: Long? = null,
        notes: String? = null
    )

    @Query("SELECT COUNT(*) FROM commitments WHERE status = 'COMPLETED'")
    suspend fun getCompletedCommitmentsCount(): Int

    @Query("SELECT COUNT(*) FROM commitments WHERE status IN ('COMPLETED', 'PARTIALLY_COMPLETED')")
    suspend fun getKeptCommitmentsCount(): Int

    @Query("SELECT COUNT(*) FROM commitments")
    suspend fun getTotalCommitmentsCount(): Int
}
