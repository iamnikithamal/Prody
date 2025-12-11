package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a letter written to one's future self.
 * These messages serve as time capsules for self-reflection and commitment.
 */
@Entity(
    tableName = "future_self_letters",
    indices = [
        Index(value = ["deliveryDate"]),
        Index(value = ["isDelivered"]),
        Index(value = ["isOpened"]),
        Index(value = ["createdAt"])
    ]
)
@Serializable
data class FutureSelfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val content: String,
    val subject: String? = null,

    // Delivery scheduling
    val deliveryDate: Long, // When the letter should be unlocked
    val isDelivered: Boolean = false, // Has the delivery date passed?
    val isOpened: Boolean = false, // Has the user opened/read it?
    val openedAt: Long? = null,

    // Context at time of writing
    val moodAtWriting: Mood = Mood.NEUTRAL,
    val goalsIncluded: String? = null, // Comma-separated goals/promises made
    val currentChallenges: String? = null, // What they were struggling with

    // Reflection after opening
    val reflectionAfterOpening: String? = null, // User's thoughts after reading
    val goalsAchieved: String? = null, // Which goals were achieved
    val achievementScore: Int? = null, // Self-rated 1-10 how well they did

    // AI analysis
    val aiAnalysis: String? = null, // Buddha's reflection on the letter
    val aiEncouragement: String? = null, // Encouragement message when opened

    val notificationScheduled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents scheduled promises/commitments extracted from future self letters.
 */
@Entity(
    tableName = "commitments",
    indices = [
        Index(value = ["letterId"]),
        Index(value = ["targetDate"]),
        Index(value = ["status"])
    ]
)
@Serializable
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val letterId: Long, // Reference to FutureSelfEntity
    val commitment: String,
    val targetDate: Long? = null,
    val status: CommitmentStatus = CommitmentStatus.PENDING,
    val completedAt: Long? = null,
    val notes: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class CommitmentStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    PARTIALLY_COMPLETED,
    NOT_ACHIEVED
}
