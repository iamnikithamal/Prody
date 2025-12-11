package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.LearningStatus
import com.prody.prashant.data.local.entity.VocabularyEntity
import com.prody.prashant.data.local.entity.VocabularyType
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    // ==================== INSERT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vocabulary: VocabularyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vocabularies: List<VocabularyEntity>): List<Long>

    // ==================== UPDATE ====================

    @Update
    suspend fun update(vocabulary: VocabularyEntity)

    @Query("UPDATE vocabulary SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE vocabulary SET
            learningStatus = :status,
            reviewCount = reviewCount + 1,
            correctCount = correctCount + :correct,
            easeFactor = :easeFactor,
            intervalDays = :intervalDays,
            nextReviewDate = :nextReviewDate,
            lastReviewDate = :lastReviewDate,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateReviewProgress(
        id: Long,
        status: LearningStatus,
        correct: Int,
        easeFactor: Float,
        intervalDays: Int,
        nextReviewDate: Long,
        lastReviewDate: Long = System.currentTimeMillis(),
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE vocabulary SET aiMnemonic = :mnemonic, aiUsageNotes = :usageNotes, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateAiContent(id: Long, mnemonic: String?, usageNotes: String?, timestamp: Long = System.currentTimeMillis())

    // ==================== DELETE ====================

    @Delete
    suspend fun delete(vocabulary: VocabularyEntity)

    @Query("DELETE FROM vocabulary WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM vocabulary")
    suspend fun deleteAll()

    // ==================== QUERY - Single Items ====================

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getById(id: Long): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    fun observeById(id: Long): Flow<VocabularyEntity?>

    @Query("SELECT * FROM vocabulary WHERE word = :word LIMIT 1")
    suspend fun getByWord(word: String): VocabularyEntity?

    @Query("SELECT * FROM vocabulary ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVocabulary(): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE type = :type ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomByType(type: VocabularyType): VocabularyEntity?

    // ==================== QUERY - Lists ====================

    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE type = :type ORDER BY createdAt DESC")
    fun observeByType(type: VocabularyType): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE category = :category ORDER BY createdAt DESC")
    fun observeByCategory(category: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE learningStatus = :status ORDER BY nextReviewDate ASC")
    fun observeByLearningStatus(status: LearningStatus): Flow<List<VocabularyEntity>>

    // ==================== QUERY - Due for Review ====================

    @Query("""
        SELECT * FROM vocabulary
        WHERE nextReviewDate <= :currentTime
        AND learningStatus != 'MASTERED'
        ORDER BY nextReviewDate ASC
        LIMIT :limit
    """)
    suspend fun getDueForReview(currentTime: Long = System.currentTimeMillis(), limit: Int = 20): List<VocabularyEntity>

    @Query("""
        SELECT * FROM vocabulary
        WHERE nextReviewDate <= :currentTime
        AND learningStatus != 'MASTERED'
        ORDER BY nextReviewDate ASC
    """)
    fun observeDueForReview(currentTime: Long = System.currentTimeMillis()): Flow<List<VocabularyEntity>>

    @Query("""
        SELECT COUNT(*) FROM vocabulary
        WHERE nextReviewDate <= :currentTime
        AND learningStatus != 'MASTERED'
    """)
    fun observeDueCount(currentTime: Long = System.currentTimeMillis()): Flow<Int>

    // ==================== QUERY - Word of the Day ====================

    @Query("""
        SELECT * FROM vocabulary
        WHERE type = :type
        AND lastReviewDate IS NULL OR lastReviewDate < :startOfDay
        ORDER BY RANDOM()
        LIMIT 1
    """)
    suspend fun getWordOfDay(type: VocabularyType = VocabularyType.WORD, startOfDay: Long): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE type = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByType(type: VocabularyType): VocabularyEntity?

    // ==================== QUERY - Search ====================

    @Query("""
        SELECT * FROM vocabulary
        WHERE word LIKE '%' || :query || '%'
        OR meaning LIKE '%' || :query || '%'
        OR example LIKE '%' || :query || '%'
        ORDER BY
            CASE WHEN word LIKE :query || '%' THEN 0 ELSE 1 END,
            word ASC
    """)
    fun search(query: String): Flow<List<VocabularyEntity>>

    // ==================== QUERY - Statistics ====================

    @Query("SELECT COUNT(*) FROM vocabulary")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE type = :type")
    fun observeCountByType(type: VocabularyType): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE learningStatus = :status")
    fun observeCountByStatus(status: LearningStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE learningStatus = 'MASTERED'")
    suspend fun getMasteredCount(): Int

    @Query("SELECT COUNT(*) FROM vocabulary WHERE createdAt >= :since")
    suspend fun getLearnedCountSince(since: Long): Int

    @Query("SELECT DISTINCT category FROM vocabulary ORDER BY category")
    fun observeCategories(): Flow<List<String>>

    // ==================== QUERY - Bulk Operations ====================

    @Query("SELECT * FROM vocabulary WHERE type = :type")
    suspend fun getAllByType(type: VocabularyType): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary WHERE learningStatus = 'NEW' ORDER BY RANDOM() LIMIT :limit")
    suspend fun getNewVocabulary(limit: Int = 10): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<VocabularyEntity>
}
