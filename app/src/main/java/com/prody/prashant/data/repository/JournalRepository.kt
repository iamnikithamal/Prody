package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.JournalDao
import com.prody.prashant.data.local.dao.MoodCount
import com.prody.prashant.data.local.dao.TimeOfDayCount
import com.prody.prashant.data.local.entity.JournalEntity
import com.prody.prashant.data.local.entity.Mood
import com.prody.prashant.data.local.entity.TimeOfDay
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository for journal-related operations.
 * Handles journal entries and their AI analysis results.
 */
class JournalRepository(
    private val journalDao: JournalDao
) {

    // ==================== Observe Operations ====================

    fun observeAll(): Flow<List<JournalEntity>> = journalDao.observeAll()

    fun observeRecent(limit: Int = 10): Flow<List<JournalEntity>> = journalDao.observeRecent(limit)

    fun observeFavorites(): Flow<List<JournalEntity>> = journalDao.observeFavorites()

    fun observeByMood(mood: Mood): Flow<List<JournalEntity>> = journalDao.observeByMood(mood)

    fun observeByTimeOfDay(timeOfDay: TimeOfDay): Flow<List<JournalEntity>> =
        journalDao.observeByTimeOfDay(timeOfDay)

    fun observeUnanalyzed(): Flow<List<JournalEntity>> = journalDao.observeUnanalyzed()

    fun observeByDateRange(startTime: Long, endTime: Long): Flow<List<JournalEntity>> =
        journalDao.observeByDateRange(startTime, endTime)

    fun observeTotalCount(): Flow<Int> = journalDao.observeTotalCount()

    fun observeTotalWords(): Flow<Int?> = journalDao.observeTotalWords()

    fun observeCountByMood(mood: Mood): Flow<Int> = journalDao.observeCountByMood(mood)

    // ==================== Search ====================

    fun search(query: String): Flow<List<JournalEntity>> = journalDao.search(query)

    fun searchByTag(tag: String): Flow<List<JournalEntity>> = journalDao.searchByTag(tag)

    // ==================== Single Item Operations ====================

    suspend fun getById(id: Long): JournalEntity? = journalDao.getById(id)

    fun observeById(id: Long): Flow<JournalEntity?> = journalDao.observeById(id)

    suspend fun getLatest(): JournalEntity? = journalDao.getLatest()

    fun observeLatest(): Flow<JournalEntity?> = journalDao.observeLatest()

    // ==================== Date-based Operations ====================

    suspend fun getTodayEntries(): List<JournalEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis
        return journalDao.getByDate(startOfDay, endOfDay)
    }

    suspend fun getEntriesSince(since: Long): List<JournalEntity> =
        journalDao.getEntriesSince(since)

    suspend fun getThisWeekEntries(): List<JournalEntity> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return journalDao.getEntriesSince(calendar.timeInMillis)
    }

    // ==================== Statistics ====================

    suspend fun getTotalCount(): Int = journalDao.getTotalCount()

    suspend fun getTotalWords(): Int = journalDao.getTotalWords() ?: 0

    suspend fun getAverageWordCount(): Float = journalDao.getAverageWordCount() ?: 0f

    suspend fun getCountSince(since: Long): Int = journalDao.getCountSince(since)

    suspend fun getWordCountSince(since: Long): Int = journalDao.getWordCountSince(since) ?: 0

    suspend fun getMoodDistribution(): List<MoodCount> = journalDao.getMoodDistribution()

    suspend fun getMoodDistributionSince(since: Long): List<MoodCount> =
        journalDao.getMoodDistributionSince(since)

    suspend fun getWritingTimeDistribution(): List<TimeOfDayCount> =
        journalDao.getWritingTimeDistribution()

    suspend fun getAllTags(): List<String> = journalDao.getAllTags()
        .flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()

    // ==================== AI Context ====================

    suspend fun getRecentForContext(limit: Int = 5): List<JournalEntity> =
        journalDao.getRecentForContext(limit)

    suspend fun getRecentSummaries(limit: Int = 5): List<String> =
        journalDao.getRecentSummaries(limit)

    // ==================== CRUD Operations ====================

    suspend fun insert(journal: JournalEntity): Long {
        val wordCount = journal.content.split(Regex("\\s+")).size
        val entryWithWordCount = journal.copy(wordCount = wordCount)
        return journalDao.insert(entryWithWordCount)
    }

    suspend fun insertAll(journals: List<JournalEntity>): List<Long> {
        val entriesWithWordCount = journals.map { journal ->
            val wordCount = journal.content.split(Regex("\\s+")).size
            journal.copy(wordCount = wordCount)
        }
        return journalDao.insertAll(entriesWithWordCount)
    }

    suspend fun update(journal: JournalEntity) {
        val wordCount = journal.content.split(Regex("\\s+")).size
        val updatedJournal = journal.copy(
            wordCount = wordCount,
            updatedAt = System.currentTimeMillis()
        )
        journalDao.update(updatedJournal)
    }

    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) =
        journalDao.updateFavoriteStatus(id, isFavorite)

    suspend fun updateMood(id: Long, mood: Mood) = journalDao.updateMood(id, mood)

    suspend fun updateTags(id: Long, tags: String?) = journalDao.updateTags(id, tags)

    suspend fun updateAiAnalysis(
        id: Long,
        summary: String?,
        reflection: String?,
        suggestion: String?,
        sentiment: String?,
        keyThemes: String?
    ) = journalDao.updateAiAnalysis(id, summary, reflection, suggestion, sentiment, keyThemes)

    suspend fun delete(journal: JournalEntity) = journalDao.delete(journal)

    suspend fun deleteById(id: Long) = journalDao.deleteById(id)

    suspend fun deleteAll() = journalDao.deleteAll()

    // ==================== Writing Streak Helper ====================

    suspend fun hasWrittenToday(): Boolean {
        val todayEntries = getTodayEntries()
        return todayEntries.isNotEmpty()
    }

    suspend fun getWritingStreakDays(): Int {
        val calendar = Calendar.getInstance()
        var streak = 0
        var currentDate = calendar.timeInMillis

        while (true) {
            calendar.timeInMillis = currentDate
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis

            val entries = journalDao.getByDate(startOfDay, endOfDay)
            if (entries.isNotEmpty()) {
                streak++
                currentDate = startOfDay - 1 // Move to previous day
            } else {
                break
            }
        }
        return streak
    }

    // ==================== Export Operations ====================

    suspend fun getTotalJournalCount(): Int = journalDao.getTotalCount()

    suspend fun getAllJournals(): List<JournalEntity> = journalDao.getAll()
}
