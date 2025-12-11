package com.prody.prashant.data.local.dao

import androidx.room.*
import com.prody.prashant.data.local.entity.BuddhaConversationEntity
import com.prody.prashant.data.local.entity.BuddhaMessageEntity
import com.prody.prashant.data.local.entity.MessageRole
import kotlinx.coroutines.flow.Flow

@Dao
interface BuddhaDao {

    // ==================== Conversations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: BuddhaConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: BuddhaConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: BuddhaConversationEntity)

    @Query("DELETE FROM buddha_conversations WHERE id = :id")
    suspend fun deleteConversationById(id: Long)

    @Query("SELECT * FROM buddha_conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): BuddhaConversationEntity?

    @Query("SELECT * FROM buddha_conversations WHERE id = :id")
    fun observeConversationById(id: Long): Flow<BuddhaConversationEntity?>

    @Query("SELECT * FROM buddha_conversations WHERE isArchived = 0 ORDER BY lastMessageAt DESC")
    fun observeActiveConversations(): Flow<List<BuddhaConversationEntity>>

    @Query("SELECT * FROM buddha_conversations WHERE isArchived = 1 ORDER BY lastMessageAt DESC")
    fun observeArchivedConversations(): Flow<List<BuddhaConversationEntity>>

    @Query("SELECT * FROM buddha_conversations ORDER BY lastMessageAt DESC")
    fun observeAllConversations(): Flow<List<BuddhaConversationEntity>>

    @Query("SELECT * FROM buddha_conversations ORDER BY lastMessageAt DESC LIMIT 1")
    suspend fun getLatestConversation(): BuddhaConversationEntity?

    @Query("SELECT * FROM buddha_conversations ORDER BY lastMessageAt DESC LIMIT 1")
    fun observeLatestConversation(): Flow<BuddhaConversationEntity?>

    @Query("""
        UPDATE buddha_conversations
        SET messageCount = messageCount + 1,
            lastMessageAt = :timestamp,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun incrementMessageCount(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE buddha_conversations
        SET title = :title,
            summary = :summary,
            mainTheme = :theme,
            tags = :tags,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateConversationMetadata(
        id: Long,
        title: String?,
        summary: String?,
        theme: String?,
        tags: String?,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE buddha_conversations SET isArchived = :isArchived, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateArchiveStatus(id: Long, isArchived: Boolean, timestamp: Long = System.currentTimeMillis())

    // ==================== Messages ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: BuddhaMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<BuddhaMessageEntity>): List<Long>

    @Update
    suspend fun updateMessage(message: BuddhaMessageEntity)

    @Delete
    suspend fun deleteMessage(message: BuddhaMessageEntity)

    @Query("DELETE FROM buddha_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM buddha_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversation(conversationId: Long)

    @Query("SELECT * FROM buddha_messages WHERE id = :id")
    suspend fun getMessageById(id: Long): BuddhaMessageEntity?

    @Query("SELECT * FROM buddha_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessagesByConversation(conversationId: Long): Flow<List<BuddhaMessageEntity>>

    @Query("SELECT * FROM buddha_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesByConversation(conversationId: Long): List<BuddhaMessageEntity>

    @Query("""
        SELECT * FROM buddha_messages
        WHERE conversationId = :conversationId
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentMessages(conversationId: Long, limit: Int = 10): List<BuddhaMessageEntity>

    @Query("SELECT * FROM buddha_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(conversationId: Long): BuddhaMessageEntity?

    // ==================== Statistics ====================

    @Query("SELECT COUNT(*) FROM buddha_conversations")
    fun observeTotalConversationCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM buddha_conversations")
    suspend fun getTotalConversationCount(): Int

    @Query("SELECT COUNT(*) FROM buddha_messages")
    fun observeTotalMessageCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM buddha_messages")
    suspend fun getTotalMessageCount(): Int

    @Query("SELECT COUNT(*) FROM buddha_messages WHERE role = :role")
    suspend fun getMessageCountByRole(role: MessageRole): Int

    @Query("SELECT COUNT(*) FROM buddha_messages WHERE conversationId = :conversationId")
    suspend fun getMessageCountForConversation(conversationId: Long): Int

    @Query("SELECT COUNT(*) FROM buddha_conversations WHERE createdAt >= :since")
    suspend fun getConversationCountSince(since: Long): Int

    @Query("SELECT COUNT(*) FROM buddha_messages WHERE timestamp >= :since")
    suspend fun getMessageCountSince(since: Long): Int

    @Query("SELECT AVG(messageCount) FROM buddha_conversations WHERE messageCount > 0")
    suspend fun getAverageMessagesPerConversation(): Float?

    // ==================== Search ====================

    @Query("""
        SELECT DISTINCT c.* FROM buddha_conversations c
        INNER JOIN buddha_messages m ON c.id = m.conversationId
        WHERE m.content LIKE '%' || :query || '%'
        OR c.title LIKE '%' || :query || '%'
        OR c.summary LIKE '%' || :query || '%'
        ORDER BY c.lastMessageAt DESC
    """)
    fun searchConversations(query: String): Flow<List<BuddhaConversationEntity>>

    @Query("""
        SELECT * FROM buddha_messages
        WHERE content LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchMessages(query: String): Flow<List<BuddhaMessageEntity>>

    // ==================== Context for AI ====================

    @Query("""
        SELECT * FROM buddha_messages
        WHERE conversationId = :conversationId
        AND role != 'SYSTEM'
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    suspend fun getRecentContextMessages(conversationId: Long, limit: Int = 20): List<BuddhaMessageEntity>

    @Query("""
        SELECT mainTheme FROM buddha_conversations
        WHERE mainTheme IS NOT NULL
        ORDER BY lastMessageAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentThemes(limit: Int = 5): List<String>

    // ==================== Cleanup ====================

    @Query("DELETE FROM buddha_conversations WHERE isArchived = 1 AND updatedAt < :beforeTimestamp")
    suspend fun deleteOldArchivedConversations(beforeTimestamp: Long): Int

    @Transaction
    suspend fun deleteConversationWithMessages(conversationId: Long) {
        deleteMessagesByConversation(conversationId)
        deleteConversationById(conversationId)
    }
}
