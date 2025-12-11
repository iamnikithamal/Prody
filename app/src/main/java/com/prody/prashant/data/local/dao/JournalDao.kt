package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.JournalEntity
import com.prody.prashant.data.local.entity.Mood
import com.prody.prashant.data.local.entity.TimeOfDay
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    // ==================== INSERT ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: JournalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(journals: List<JournalEntity>): List<Long>

    // ==================== UPDATE ====================

    @Update
    suspend fun update(journal: JournalEntity)

    @Query("UPDATE journal_entries SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE journal_entries SET
            isAnalyzed = 1,
            aiSummary = :summary,
            aiReflection = :reflection,
            aiSuggestion = :suggestion,
            aiSentiment = :sentiment,
            aiKeyThemes = :keyThemes,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateAiAnalysis(
        id: Long,
        summary: String?,
        reflection: String?,
        suggestion: String?,
        sentiment: String?,
        keyThemes: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE journal_entries SET mood = :mood, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateMood(id: Long, mood: Mood, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE journal_entries SET tags = :tags, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTags(id: Long, tags: String?, timestamp: Long = System.currentTimeMillis())

    // ==================== DELETE ====================

    @Delete
    suspend fun delete(journal: JournalEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAll()

    // ==================== QUERY - Single Items ====================

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getById(id: Long): JournalEntity?

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    fun observeById(id: Long): Flow<JournalEntity?>

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatest(): JournalEntity?

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC LIMIT 1")
    fun observeLatest(): Flow<JournalEntity?>

    // ==================== QUERY - Lists ====================

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 10): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE mood = :mood ORDER BY createdAt DESC")
    fun observeByMood(mood: Mood): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE timeOfDay = :timeOfDay ORDER BY createdAt DESC")
    fun observeByTimeOfDay(timeOfDay: TimeOfDay): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE isAnalyzed = 0 ORDER BY createdAt DESC")
    fun observeUnanalyzed(): Flow<List<JournalEntity>>

    // ==================== QUERY - Date Range ====================

    @Query("SELECT * FROM journal_entries WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
    fun observeByDateRange(startTime: Long, endTime: Long): Flow<List<JournalEntity>>

    @Query("SELECT * FROM journal_entries WHERE createdAt >= :startOfDay AND createdAt < :endOfDay ORDER BY createdAt DESC")
    suspend fun getByDate(startOfDay: Long, endOfDay: Long): List<JournalEntity>

    @Query("SELECT * FROM journal_entries WHERE createdAt >= :since ORDER BY createdAt DESC")
    suspend fun getEntriesSince(since: Long): List<JournalEntity>

    // ==================== QUERY - Search ====================

    @Query("""
        SELECT * FROM journal_entries
        WHERE content LIKE '%' || :query || '%'
        OR title LIKE '%' || :query || '%'
        OR tags LIKE '%' || :query || '%'
        OR aiKeyThemes LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<JournalEntity>>

    @Query("""
        SELECT * FROM journal_entries
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY createdAt DESC
    """)
    fun searchByTag(tag: String): Flow<List<JournalEntity>>

    // ==================== QUERY - Statistics ====================

    @Query("SELECT COUNT(*) FROM journal_entries")
    fun observeTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getTotalCount(): Int

    @Query("SELECT SUM(wordCount) FROM journal_entries")
    fun observeTotalWords(): Flow<Int?>

    @Query("SELECT SUM(wordCount) FROM journal_entries")
    suspend fun getTotalWords(): Int?

    @Query("SELECT AVG(wordCount) FROM journal_entries")
    suspend fun getAverageWordCount(): Float?

    @Query("SELECT COUNT(*) FROM journal_entries WHERE mood = :mood")
    fun observeCountByMood(mood: Mood): Flow<Int>

    @Query("SELECT COUNT(*) FROM journal_entries WHERE createdAt >= :since")
    suspend fun getCountSince(since: Long): Int

    @Query("SELECT SUM(wordCount) FROM journal_entries WHERE createdAt >= :since")
    suspend fun getWordCountSince(since: Long): Int?

    // ==================== QUERY - Mood Distribution ====================

    @Query("""
        SELECT mood, COUNT(*) as count FROM journal_entries
        GROUP BY mood
        ORDER BY count DESC
    """)
    suspend fun getMoodDistribution(): List<MoodCount>

    @Query("""
        SELECT mood, COUNT(*) as count FROM journal_entries
        WHERE createdAt >= :since
        GROUP BY mood
        ORDER BY count DESC
    """)
    suspend fun getMoodDistributionSince(since: Long): List<MoodCount>

    // ==================== QUERY - Writing Patterns ====================

    @Query("""
        SELECT timeOfDay, COUNT(*) as count FROM journal_entries
        GROUP BY timeOfDay
        ORDER BY count DESC
    """)
    suspend fun getWritingTimeDistribution(): List<TimeOfDayCount>

    @Query("SELECT DISTINCT tags FROM journal_entries WHERE tags IS NOT NULL")
    suspend fun getAllTags(): List<String>

    // ==================== QUERY - For AI Context ====================

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentForContext(limit: Int = 5): List<JournalEntity>

    @Query("SELECT aiSummary FROM journal_entries WHERE aiSummary IS NOT NULL ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSummaries(limit: Int = 5): List<String>

    // ==================== Export ====================

    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    suspend fun getAll(): List<JournalEntity>
}

data class MoodCount(
    val mood: Mood,
    val count: Int
)

data class TimeOfDayCount(
    val timeOfDay: TimeOfDay,
    val count: Int
)
