package com.prody.prashant.data.repository

import com.prody.prashant.data.local.dao.BuddhaDao
import com.prody.prashant.data.local.entity.BuddhaConversationEntity
import com.prody.prashant.data.local.entity.BuddhaMessageEntity
import com.prody.prashant.data.local.entity.MessageRole
import kotlinx.coroutines.flow.Flow

/**
 * Repository for Buddha AI conversation operations.
 * Manages conversations and messages with the Buddha AI mentor.
 */
class BuddhaRepository(
    private val buddhaDao: BuddhaDao
) {

    // ==================== Conversation Observe Operations ====================

    fun observeActiveConversations(): Flow<List<BuddhaConversationEntity>> =
        buddhaDao.observeActiveConversations()

    fun observeArchivedConversations(): Flow<List<BuddhaConversationEntity>> =
        buddhaDao.observeArchivedConversations()

    fun observeAllConversations(): Flow<List<BuddhaConversationEntity>> =
        buddhaDao.observeAllConversations()

    fun observeConversationById(id: Long): Flow<BuddhaConversationEntity?> =
        buddhaDao.observeConversationById(id)

    fun observeLatestConversation(): Flow<BuddhaConversationEntity?> =
        buddhaDao.observeLatestConversation()

    fun observeTotalConversationCount(): Flow<Int> = buddhaDao.observeTotalConversationCount()

    fun observeTotalMessageCount(): Flow<Int> = buddhaDao.observeTotalMessageCount()

    // ==================== Conversation Single Item Operations ====================

    suspend fun getConversationById(id: Long): BuddhaConversationEntity? =
        buddhaDao.getConversationById(id)

    suspend fun getLatestConversation(): BuddhaConversationEntity? =
        buddhaDao.getLatestConversation()

    suspend fun getOrCreateConversation(): BuddhaConversationEntity {
        return getLatestConversation() ?: run {
            val newConversation = BuddhaConversationEntity()
            val id = buddhaDao.insertConversation(newConversation)
            newConversation.copy(id = id)
        }
    }

    // ==================== Conversation CRUD Operations ====================

    suspend fun createConversation(title: String? = null): Long {
        val conversation = BuddhaConversationEntity(title = title)
        return buddhaDao.insertConversation(conversation)
    }

    suspend fun updateConversation(conversation: BuddhaConversationEntity) =
        buddhaDao.updateConversation(conversation)

    suspend fun deleteConversation(conversation: BuddhaConversationEntity) =
        buddhaDao.deleteConversation(conversation)

    suspend fun deleteConversationById(id: Long) =
        buddhaDao.deleteConversationById(id)

    suspend fun deleteConversationWithMessages(conversationId: Long) =
        buddhaDao.deleteConversationWithMessages(conversationId)

    suspend fun updateConversationMetadata(
        id: Long,
        title: String?,
        summary: String?,
        theme: String?,
        tags: String?
    ) = buddhaDao.updateConversationMetadata(id, title, summary, theme, tags)

    suspend fun archiveConversation(id: Long) =
        buddhaDao.updateArchiveStatus(id, isArchived = true)

    suspend fun unarchiveConversation(id: Long) =
        buddhaDao.updateArchiveStatus(id, isArchived = false)

    // ==================== Message Observe Operations ====================

    fun observeMessagesByConversation(conversationId: Long): Flow<List<BuddhaMessageEntity>> =
        buddhaDao.observeMessagesByConversation(conversationId)

    // ==================== Message Operations ====================

    suspend fun getMessagesByConversation(conversationId: Long): List<BuddhaMessageEntity> =
        buddhaDao.getMessagesByConversation(conversationId)

    suspend fun getRecentMessages(conversationId: Long, limit: Int = 10): List<BuddhaMessageEntity> =
        buddhaDao.getRecentMessages(conversationId, limit)

    suspend fun getLastMessage(conversationId: Long): BuddhaMessageEntity? =
        buddhaDao.getLastMessage(conversationId)

    suspend fun getMessageById(id: Long): BuddhaMessageEntity? =
        buddhaDao.getMessageById(id)

    /**
     * Adds a user message to a conversation.
     */
    suspend fun addUserMessage(
        conversationId: Long,
        content: String,
        relatedJournalId: Long? = null,
        relatedVocabularyId: Long? = null
    ): Long {
        val message = BuddhaMessageEntity(
            conversationId = conversationId,
            content = content,
            role = MessageRole.USER,
            relatedJournalId = relatedJournalId,
            relatedVocabularyId = relatedVocabularyId
        )
        val messageId = buddhaDao.insertMessage(message)
        buddhaDao.incrementMessageCount(conversationId)
        return messageId
    }

    /**
     * Adds Buddha's response to a conversation.
     */
    suspend fun addBuddhaMessage(
        conversationId: Long,
        content: String,
        tokenCount: Int? = null,
        responseTimeMs: Long? = null,
        modelUsed: String? = null
    ): Long {
        val message = BuddhaMessageEntity(
            conversationId = conversationId,
            content = content,
            role = MessageRole.BUDDHA,
            tokenCount = tokenCount,
            responseTimeMs = responseTimeMs,
            modelUsed = modelUsed
        )
        val messageId = buddhaDao.insertMessage(message)
        buddhaDao.incrementMessageCount(conversationId)
        return messageId
    }

    /**
     * Adds a system message (for context, not displayed to user).
     */
    suspend fun addSystemMessage(conversationId: Long, content: String): Long {
        val message = BuddhaMessageEntity(
            conversationId = conversationId,
            content = content,
            role = MessageRole.SYSTEM
        )
        return buddhaDao.insertMessage(message)
    }

    suspend fun deleteMessage(message: BuddhaMessageEntity) =
        buddhaDao.deleteMessage(message)

    suspend fun deleteMessageById(id: Long) =
        buddhaDao.deleteMessageById(id)

    // ==================== Statistics ====================

    suspend fun getTotalConversationCount(): Int = buddhaDao.getTotalConversationCount()

    suspend fun getTotalMessageCount(): Int = buddhaDao.getTotalMessageCount()

    suspend fun getMessageCountByRole(role: MessageRole): Int =
        buddhaDao.getMessageCountByRole(role)

    suspend fun getMessageCountForConversation(conversationId: Long): Int =
        buddhaDao.getMessageCountForConversation(conversationId)

    suspend fun getConversationCountSince(since: Long): Int =
        buddhaDao.getConversationCountSince(since)

    suspend fun getMessageCountSince(since: Long): Int =
        buddhaDao.getMessageCountSince(since)

    suspend fun getAverageMessagesPerConversation(): Float =
        buddhaDao.getAverageMessagesPerConversation() ?: 0f

    // ==================== Search ====================

    fun searchConversations(query: String): Flow<List<BuddhaConversationEntity>> =
        buddhaDao.searchConversations(query)

    fun searchMessages(query: String): Flow<List<BuddhaMessageEntity>> =
        buddhaDao.searchMessages(query)

    // ==================== Context for AI ====================

    /**
     * Gets recent context messages for AI (excludes system messages).
     */
    suspend fun getRecentContextMessages(conversationId: Long, limit: Int = 20): List<BuddhaMessageEntity> =
        buddhaDao.getRecentContextMessages(conversationId, limit)

    suspend fun getRecentThemes(limit: Int = 5): List<String> =
        buddhaDao.getRecentThemes(limit)

    /**
     * Builds conversation history for AI context.
     * Returns a formatted string of recent messages.
     */
    suspend fun buildConversationContext(conversationId: Long, messageLimit: Int = 10): String {
        val messages = getRecentContextMessages(conversationId, messageLimit)
            .reversed() // Oldest first

        if (messages.isEmpty()) return ""

        return messages.joinToString("\n\n") { message ->
            when (message.role) {
                MessageRole.USER -> "User: ${message.content}"
                MessageRole.BUDDHA -> "Buddha: ${message.content}"
                MessageRole.SYSTEM -> "" // Exclude system messages from context
            }
        }.trim()
    }

    // ==================== Cleanup ====================

    suspend fun deleteOldArchivedConversations(beforeDays: Int = 30): Int {
        val beforeTimestamp = System.currentTimeMillis() - (beforeDays * 24 * 60 * 60 * 1000L)
        return buddhaDao.deleteOldArchivedConversations(beforeTimestamp)
    }

    // ==================== Export Operations ====================

    suspend fun getConversationCount(): Int = getTotalConversationCount()
}
