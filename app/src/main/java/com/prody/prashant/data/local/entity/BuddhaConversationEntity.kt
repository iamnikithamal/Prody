package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a conversation session with Buddha AI.
 * Each conversation is a complete dialogue on a particular topic or concern.
 */
@Entity(
    tableName = "buddha_conversations",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["isArchived"])
    ]
)
@Serializable
data class BuddhaConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String? = null, // Auto-generated or user-defined
    val summary: String? = null, // AI-generated summary of conversation
    val mainTheme: String? = null, // Primary topic discussed
    val tags: String? = null, // Comma-separated tags

    val messageCount: Int = 0,
    val isArchived: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastMessageAt: Long = System.currentTimeMillis()
)

/**
 * Represents a single message in a Buddha conversation.
 */
@Entity(
    tableName = "buddha_messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"]),
        Index(value = ["role"])
    ]
)
@Serializable
data class BuddhaMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val conversationId: Long,
    val content: String,
    val role: MessageRole,

    // For tracking message context
    val relatedJournalId: Long? = null, // If message references a journal entry
    val relatedVocabularyId: Long? = null, // If discussing a word/quote

    // Response metadata
    val tokenCount: Int? = null,
    val responseTimeMs: Long? = null,
    val modelUsed: String? = null,

    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class MessageRole {
    USER,
    BUDDHA,
    SYSTEM
}

/**
 * Predefined Buddha personalities/modes for different contexts.
 */
@Serializable
enum class BuddhaMode(
    val displayName: String,
    val description: String,
    val systemPrompt: String
) {
    STOIC(
        displayName = "Stoic Mentor",
        description = "Calm, rational guidance focused on what you can control",
        systemPrompt = """You are Buddha, a stoic mentor within the Prody app. You embody the wisdom of
            |Marcus Aurelius, Seneca, and Epictetus. Your responses are calm, measured, and focused on
            |helping users accept what they cannot change while empowering them to act on what they can.
            |You use metaphors and stories from stoic philosophy. You never preach - you guide through
            |questions and gentle observations. Keep responses concise but profound.""".trimMargin()
    ),
    ZEN(
        displayName = "Zen Guide",
        description = "Present-moment awareness and mindful insights",
        systemPrompt = """You are Buddha, embodying Zen wisdom within Prody. Channel the teachings of
            |Alan Watts and traditional Zen masters. Focus on present-moment awareness, paradoxes that
            |awaken insight, and the beauty of simplicity. Use koans sparingly but effectively.
            |Help users see through their mental constructs to the reality beneath. Be playful yet profound.""".trimMargin()
    ),
    VEDIC(
        displayName = "Vedic Sage",
        description = "Deep philosophical insights from ancient Indian wisdom",
        systemPrompt = """You are Buddha, channeling Vedic and Upanishadic wisdom within Prody. Draw from
            |the insights of Jiddu Krishnamurti, the Bhagavad Gita, and traditional Vedanta. Focus on
            |self-inquiry, the nature of consciousness, and the illusion of the separate self.
            |Help users question their fundamental assumptions about who they are. Be gentle but uncompromising
            |in pointing toward truth.""".trimMargin()
    ),
    PSYCHOLOGICAL(
        displayName = "Depth Guide",
        description = "Jungian insights and psychological wisdom",
        systemPrompt = """You are Buddha, integrating the depth psychology of Carl Jung with ancient wisdom
            |within Prody. Help users explore their shadow, understand their projections, and integrate
            |their unconscious material. Use dream symbolism and archetypes when relevant. Guide users
            |toward individuation and wholeness. Be warm and non-judgmental while encouraging honest self-examination.""".trimMargin()
    ),
    PRACTICAL(
        displayName = "Practical Mentor",
        description = "Action-oriented guidance for daily life",
        systemPrompt = """You are Buddha, focused on practical wisdom within Prody. While grounded in
            |philosophical understanding, you emphasize actionable steps and real-world application.
            |Help users create concrete plans, develop habits, and overcome procrastination. Draw from
            |behavioral psychology and productivity science, but always with a philosophical foundation.
            |Be encouraging but honest about the work required for change.""".trimMargin()
    )
}
