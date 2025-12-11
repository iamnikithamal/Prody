package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.FutureSelfDao
import com.prody.prashant.data.local.entity.CommitmentEntity
import com.prody.prashant.data.local.entity.CommitmentStatus
import com.prody.prashant.data.local.entity.FutureSelfEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository for future self letter and commitment operations.
 * Manages time-capsule letters and extracted commitments.
 */
class FutureSelfRepository(
    private val futureSelfDao: FutureSelfDao
) {

    // ==================== Letter Observe Operations ====================

    fun observeAllLetters(): Flow<List<FutureSelfEntity>> = futureSelfDao.observeAllLetters()

    fun observePendingLetters(): Flow<List<FutureSelfEntity>> = futureSelfDao.observePendingLetters()

    fun observeDeliveredLetters(): Flow<List<FutureSelfEntity>> = futureSelfDao.observeDeliveredLetters()

    fun observeUnreadDeliveredLetters(): Flow<List<FutureSelfEntity>> =
        futureSelfDao.observeUnreadDeliveredLetters()

    fun observeOpenedLetters(): Flow<List<FutureSelfEntity>> = futureSelfDao.observeOpenedLetters()

    fun observeLetterById(id: Long): Flow<FutureSelfEntity?> = futureSelfDao.observeLetterById(id)

    fun observeNextUpcomingLetter(): Flow<FutureSelfEntity?> = futureSelfDao.observeNextUpcomingLetter()

    fun observeTotalCount(): Flow<Int> = futureSelfDao.observeTotalCount()

    fun observePendingCount(): Flow<Int> = futureSelfDao.observePendingCount()

    fun observeUnreadCount(): Flow<Int> = futureSelfDao.observeUnreadCount()

    fun observeOpenedCount(): Flow<Int> = futureSelfDao.observeOpenedCount()

    // ==================== Letter Single Item Operations ====================

    suspend fun getLetterById(id: Long): FutureSelfEntity? = futureSelfDao.getLetterById(id)

    suspend fun getNextUpcomingLetter(): FutureSelfEntity? = futureSelfDao.getNextUpcomingLetter()

    suspend fun getLettersDueForDelivery(): List<FutureSelfEntity> =
        futureSelfDao.getLettersDueForDelivery(System.currentTimeMillis())

    suspend fun getLettersDeliveryInRange(startTime: Long, endTime: Long): List<FutureSelfEntity> =
        futureSelfDao.getLettersDeliveryInRange(startTime, endTime)

    // ==================== Letter CRUD Operations ====================

    suspend fun insertLetter(letter: FutureSelfEntity): Long = futureSelfDao.insertLetter(letter)

    suspend fun updateLetter(letter: FutureSelfEntity) = futureSelfDao.updateLetter(letter)

    suspend fun deleteLetter(letter: FutureSelfEntity) = futureSelfDao.deleteLetter(letter)

    suspend fun deleteLetterById(id: Long) = futureSelfDao.deleteLetterById(id)

    // ==================== Letter Status Operations ====================

    /**
     * Checks for letters that are due for delivery and marks them as delivered.
     * @return Number of letters marked as delivered
     */
    suspend fun processDeliveries(): Int = futureSelfDao.markDueLettersAsDelivered()

    suspend fun markLetterAsOpened(id: Long) = futureSelfDao.markLetterAsOpened(id)

    suspend fun updateLetterReflection(
        id: Long,
        reflection: String?,
        goalsAchieved: String?,
        score: Int?
    ) = futureSelfDao.updateLetterReflection(id, reflection, goalsAchieved, score)

    suspend fun updateAiAnalysis(id: Long, analysis: String?, encouragement: String?) =
        futureSelfDao.updateAiAnalysis(id, analysis, encouragement)

    suspend fun markNotificationScheduled(id: Long) = futureSelfDao.markNotificationScheduled(id)

    // ==================== Letter Statistics ====================

    suspend fun getAverageAchievementScore(): Float = futureSelfDao.getAverageAchievementScore() ?: 0f

    suspend fun getCountSince(since: Long): Int = futureSelfDao.getCountSince(since)

    // ==================== Commitment Observe Operations ====================

    fun observeCommitmentsByLetter(letterId: Long): Flow<List<CommitmentEntity>> =
        futureSelfDao.observeCommitmentsByLetter(letterId)

    fun observeCommitmentsByStatus(status: CommitmentStatus): Flow<List<CommitmentEntity>> =
        futureSelfDao.observeCommitmentsByStatus(status)

    // ==================== Commitment CRUD Operations ====================

    suspend fun insertCommitment(commitment: CommitmentEntity): Long =
        futureSelfDao.insertCommitment(commitment)

    suspend fun insertCommitments(commitments: List<CommitmentEntity>): List<Long> =
        futureSelfDao.insertCommitments(commitments)

    suspend fun updateCommitment(commitment: CommitmentEntity) =
        futureSelfDao.updateCommitment(commitment)

    suspend fun deleteCommitment(commitment: CommitmentEntity) =
        futureSelfDao.deleteCommitment(commitment)

    suspend fun getCommitmentById(id: Long): CommitmentEntity? =
        futureSelfDao.getCommitmentById(id)

    suspend fun getCommitmentsByLetter(letterId: Long): List<CommitmentEntity> =
        futureSelfDao.getCommitmentsByLetter(letterId)

    // ==================== Commitment Status Operations ====================

    suspend fun markCommitmentCompleted(id: Long, notes: String? = null) =
        futureSelfDao.updateCommitmentStatus(
            id = id,
            status = CommitmentStatus.COMPLETED,
            completedAt = System.currentTimeMillis(),
            notes = notes
        )

    suspend fun markCommitmentPartiallyCompleted(id: Long, notes: String? = null) =
        futureSelfDao.updateCommitmentStatus(
            id = id,
            status = CommitmentStatus.PARTIALLY_COMPLETED,
            completedAt = System.currentTimeMillis(),
            notes = notes
        )

    suspend fun markCommitmentNotAchieved(id: Long, notes: String? = null) =
        futureSelfDao.updateCommitmentStatus(
            id = id,
            status = CommitmentStatus.NOT_ACHIEVED,
            notes = notes
        )

    suspend fun startCommitment(id: Long) =
        futureSelfDao.updateCommitmentStatus(
            id = id,
            status = CommitmentStatus.IN_PROGRESS
        )

    // ==================== Commitment Statistics ====================

    suspend fun getCompletedCommitmentsCount(): Int = futureSelfDao.getCompletedCommitmentsCount()

    suspend fun getKeptCommitmentsCount(): Int = futureSelfDao.getKeptCommitmentsCount()

    suspend fun getTotalCommitmentsCount(): Int = futureSelfDao.getTotalCommitmentsCount()

    suspend fun getCommitmentSuccessRate(): Float {
        val total = getTotalCommitmentsCount()
        if (total == 0) return 0f
        val kept = getKeptCommitmentsCount()
        return kept.toFloat() / total.toFloat()
    }

    // ==================== Utility Functions ====================

    /**
     * Creates a letter with default delivery date of 30 days from now.
     */
    suspend fun createLetterToFutureSelf(
        content: String,
        subject: String? = null,
        daysFromNow: Int = 30
    ): Long {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysFromNow)
            set(Calendar.HOUR_OF_DAY, 9) // Deliver at 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val letter = FutureSelfEntity(
            content = content,
            subject = subject,
            deliveryDate = calendar.timeInMillis
        )

        return insertLetter(letter)
    }

    /**
     * Gets letters that will be delivered this week.
     */
    suspend fun getLettersComingThisWeek(): List<FutureSelfEntity> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }
        return getLettersDeliveryInRange(now, calendar.timeInMillis)
    }

    /**
     * Calculates days until next letter delivery.
     */
    suspend fun getDaysUntilNextDelivery(): Int? {
        val nextLetter = getNextUpcomingLetter() ?: return null
        val now = System.currentTimeMillis()
        val diff = nextLetter.deliveryDate - now
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    // ==================== Export Operations ====================

    suspend fun getTotalLetterCount(): Int = futureSelfDao.getTotalLetterCount()
}
