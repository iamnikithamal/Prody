package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.VocabularyDao
import com.prody.prashant.data.local.entity.LearningStatus
import com.prody.prashant.data.local.entity.VocabularyEntity
import com.prody.prashant.data.local.entity.VocabularyType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Repository for vocabulary-related operations.
 * Implements SM-2 spaced repetition algorithm for effective learning.
 */
class VocabularyRepository(
    private val vocabularyDao: VocabularyDao
) {

    // ==================== Observe Operations ====================

    fun observeAll(): Flow<List<VocabularyEntity>> = vocabularyDao.observeAll()

    fun observeByType(type: VocabularyType): Flow<List<VocabularyEntity>> =
        vocabularyDao.observeByType(type)

    fun observeByCategory(category: String): Flow<List<VocabularyEntity>> =
        vocabularyDao.observeByCategory(category)

    fun observeFavorites(): Flow<List<VocabularyEntity>> = vocabularyDao.observeFavorites()

    fun observeByLearningStatus(status: LearningStatus): Flow<List<VocabularyEntity>> =
        vocabularyDao.observeByLearningStatus(status)

    fun observeDueForReview(): Flow<List<VocabularyEntity>> =
        vocabularyDao.observeDueForReview(System.currentTimeMillis())

    fun observeDueCount(): Flow<Int> =
        vocabularyDao.observeDueCount(System.currentTimeMillis())

    fun search(query: String): Flow<List<VocabularyEntity>> = vocabularyDao.search(query)

    fun observeCategories(): Flow<List<String>> = vocabularyDao.observeCategories()

    fun observeTotalCount(): Flow<Int> = vocabularyDao.observeTotalCount()

    fun observeCountByType(type: VocabularyType): Flow<Int> = vocabularyDao.observeCountByType(type)

    fun observeCountByStatus(status: LearningStatus): Flow<Int> =
        vocabularyDao.observeCountByStatus(status)

    // ==================== Single Item Operations ====================

    suspend fun getById(id: Long): VocabularyEntity? = vocabularyDao.getById(id)

    fun observeById(id: Long): Flow<VocabularyEntity?> = vocabularyDao.observeById(id)

    suspend fun getByWord(word: String): VocabularyEntity? = vocabularyDao.getByWord(word)

    suspend fun getRandomVocabulary(): VocabularyEntity? = vocabularyDao.getRandomVocabulary()

    suspend fun getRandomByType(type: VocabularyType): VocabularyEntity? =
        vocabularyDao.getRandomByType(type)

    suspend fun getWordOfDay(): VocabularyEntity? {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return vocabularyDao.getWordOfDay(startOfDay = calendar.timeInMillis)
            ?: vocabularyDao.getLatestByType(VocabularyType.WORD)
    }

    suspend fun getQuoteOfDay(): VocabularyEntity? {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return vocabularyDao.getWordOfDay(VocabularyType.QUOTE, calendar.timeInMillis)
            ?: vocabularyDao.getRandomByType(VocabularyType.QUOTE)
    }

    // ==================== Learning Operations ====================

    suspend fun getDueForReview(limit: Int = 20): List<VocabularyEntity> =
        vocabularyDao.getDueForReview(System.currentTimeMillis(), limit)

    suspend fun getNewVocabulary(limit: Int = 10): List<VocabularyEntity> =
        vocabularyDao.getNewVocabulary(limit)

    suspend fun getMasteredCount(): Int = vocabularyDao.getMasteredCount()

    suspend fun getLearnedCountSince(since: Long): Int = vocabularyDao.getLearnedCountSince(since)

    /**
     * Records a review result using the SM-2 spaced repetition algorithm.
     * @param vocabularyId The ID of the vocabulary item
     * @param quality Rating from 0-5 (0-2: incorrect, 3-5: correct with varying ease)
     */
    suspend fun recordReview(vocabularyId: Long, quality: Int) {
        val vocabulary = vocabularyDao.getById(vocabularyId) ?: return
        val clampedQuality = quality.coerceIn(0, 5)

        // SM-2 Algorithm
        val isCorrect = clampedQuality >= 3
        val newEaseFactor = calculateNewEaseFactor(vocabulary.easeFactor, clampedQuality)
        val newInterval = calculateNewInterval(
            vocabulary.intervalDays,
            vocabulary.reviewCount,
            newEaseFactor,
            isCorrect
        )
        val newStatus = calculateNewStatus(vocabulary, isCorrect)
        val nextReviewDate = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000L)

        vocabularyDao.updateReviewProgress(
            id = vocabularyId,
            status = newStatus,
            correct = if (isCorrect) 1 else 0,
            easeFactor = newEaseFactor,
            intervalDays = newInterval,
            nextReviewDate = nextReviewDate,
            lastReviewDate = System.currentTimeMillis()
        )
    }

    private fun calculateNewEaseFactor(currentEF: Float, quality: Int): Float {
        // SM-2 formula: EF' = EF + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        val newEF = currentEF + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        return max(1.3f, newEF) // Minimum EF is 1.3
    }

    private fun calculateNewInterval(
        currentInterval: Int,
        reviewCount: Int,
        easeFactor: Float,
        isCorrect: Boolean
    ): Int {
        return if (!isCorrect) {
            1 // Reset to 1 day on incorrect answer
        } else {
            when (reviewCount) {
                0 -> 1
                1 -> 6
                else -> (currentInterval * easeFactor).roundToInt()
            }
        }
    }

    private fun calculateNewStatus(vocabulary: VocabularyEntity, isCorrect: Boolean): LearningStatus {
        return if (!isCorrect) {
            LearningStatus.LEARNING
        } else {
            when {
                vocabulary.reviewCount >= 5 && vocabulary.correctCount >= 4 -> LearningStatus.MASTERED
                vocabulary.reviewCount >= 2 -> LearningStatus.REVIEWING
                else -> LearningStatus.LEARNING
            }
        }
    }

    // ==================== CRUD Operations ====================

    suspend fun insert(vocabulary: VocabularyEntity): Long = vocabularyDao.insert(vocabulary)

    suspend fun insertAll(vocabularies: List<VocabularyEntity>): List<Long> =
        vocabularyDao.insertAll(vocabularies)

    suspend fun update(vocabulary: VocabularyEntity) = vocabularyDao.update(vocabulary)

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) =
        vocabularyDao.updateFavoriteStatus(id, isFavorite)

    suspend fun updateAiContent(id: Long, mnemonic: String?, usageNotes: String?) =
        vocabularyDao.updateAiContent(id, mnemonic, usageNotes)

    suspend fun delete(vocabulary: VocabularyEntity) = vocabularyDao.delete(vocabulary)

    suspend fun deleteById(id: Long) = vocabularyDao.deleteById(id)

    suspend fun deleteAll() = vocabularyDao.deleteAll()

    // ==================== Bulk Operations ====================

    suspend fun getAllByType(type: VocabularyType): List<VocabularyEntity> =
        vocabularyDao.getAllByType(type)

    suspend fun getByIds(ids: List<Long>): List<VocabularyEntity> = vocabularyDao.getByIds(ids)
}
