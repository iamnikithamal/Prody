package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Tracks user's daily activity and streak information.
 * Used for gamification and progress visualization.
 */
@Entity(
    tableName = "daily_activity",
    indices = [
        Index(value = ["date"], unique = true)
    ]
)
@Serializable
data class DailyActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: Long, // Start of day timestamp (normalized to midnight)

    // Activity counts
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val journalEntries: Int = 0,
    val journalWords: Int = 0,
    val buddhaMessages: Int = 0,
    val futureLettersWritten: Int = 0,
    val futureLettersOpened: Int = 0,

    // Time spent (in seconds)
    val learningTimeSeconds: Int = 0,
    val journalingTimeSeconds: Int = 0,
    val buddhaTimeSeconds: Int = 0,
    val totalActiveTimeSeconds: Int = 0,

    // Session info
    val sessionsCount: Int = 0,
    val firstSessionAt: Long? = null,
    val lastSessionAt: Long? = null,

    // XP earned this day
    val xpEarned: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Overall user statistics and profile information.
 */
@Entity(tableName = "user_stats")
@Serializable
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1, // Singleton - only one user stats row

    // Profile
    val displayName: String = "Seeker",
    val bio: String? = null,
    val avatarId: String = "default",
    val bannerId: String = "default",
    val joinedAt: Long = System.currentTimeMillis(),

    // Level & XP
    val level: Int = 1,
    val currentXp: Int = 0,
    val totalXp: Int = 0,
    val levelTitle: LevelTitle = LevelTitle.NOVICE,

    // Streaks
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActiveDate: Long? = null,
    val streakStartDate: Long? = null,

    // Lifetime stats
    val totalWordsLearned: Int = 0,
    val totalWordsMastered: Int = 0,
    val totalJournalEntries: Int = 0,
    val totalJournalWords: Int = 0,
    val totalBuddhaConversations: Int = 0,
    val totalBuddhaMessages: Int = 0,
    val totalFutureLetters: Int = 0,
    val totalFutureLettersOpened: Int = 0,
    val totalCommitmentsKept: Int = 0,

    // Time stats (in seconds)
    val totalLearningTime: Int = 0,
    val totalJournalingTime: Int = 0,
    val totalBuddhaTime: Int = 0,
    val totalActiveTime: Int = 0,
    val totalDaysActive: Int = 0,

    // Achievements
    val badgesEarned: String = "", // Comma-separated badge IDs
    val unlockedAvatars: String = "default", // Comma-separated avatar IDs
    val unlockedBanners: String = "default", // Comma-separated banner IDs

    // Preferences set during onboarding
    val preferredLearningTime: String? = null, // "morning", "afternoon", "evening", "night"
    val dailyGoalWords: Int = 5,
    val dailyGoalJournalMinutes: Int = 10,
    val focusAreas: String? = null, // Comma-separated: "vocabulary", "philosophy", "journaling", etc.

    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class LevelTitle(val displayName: String, val minXp: Int, val maxXp: Int) {
    NOVICE("Novice", 0, 499),
    APPRENTICE("Apprentice", 500, 1999),
    JOURNEYMAN("Journeyman", 2000, 4999),
    EXPERT("Expert", 5000, 9999),
    MASTER("Master", 10000, 19999),
    SAGE("Sage", 20000, Int.MAX_VALUE);

    companion object {
        fun fromXp(totalXp: Int): LevelTitle = entries.last { totalXp >= it.minXp }
    }
}

/**
 * Represents an achievement/badge the user can earn.
 */
@Entity(
    tableName = "badges",
    indices = [
        Index(value = ["badgeId"], unique = true)
    ]
)
@Serializable
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val badgeId: String, // Unique identifier like "first_word", "week_streak", etc.
    val name: String,
    val description: String,
    val iconName: String, // Material icon name
    val category: BadgeCategory,
    val tier: BadgeTier = BadgeTier.BRONZE,

    val isEarned: Boolean = false,
    val earnedAt: Long? = null,
    val progress: Int = 0, // Current progress toward badge
    val requirement: Int = 1, // Total needed to earn

    // For display
    val xpReward: Int = 0,
    val unlocksAvatar: String? = null,
    val unlocksBanner: String? = null
)

@Serializable
enum class BadgeCategory {
    LEARNING,
    JOURNALING,
    BUDDHA,
    STREAKS,
    SOCIAL,
    SPECIAL
}

@Serializable
enum class BadgeTier(val displayName: String, val multiplier: Float) {
    BRONZE("Bronze", 1.0f),
    SILVER("Silver", 1.5f),
    GOLD("Gold", 2.0f),
    PLATINUM("Platinum", 3.0f),
    DIAMOND("Diamond", 5.0f)
}

/**
 * Tracks XP transactions for audit trail and detailed progress view.
 */
@Entity(
    tableName = "xp_transactions",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["source"])
    ]
)
@Serializable
data class XpTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Int,
    val source: XpSource,
    val description: String,
    val relatedEntityId: Long? = null, // ID of related vocabulary/journal/etc.

    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class XpSource(val displayName: String, val baseXp: Int) {
    WORD_LEARNED("Word Learned", 10),
    WORD_MASTERED("Word Mastered", 50),
    WORD_REVIEWED("Word Reviewed", 5),
    JOURNAL_WRITTEN("Journal Entry", 20),
    JOURNAL_LONG("Long Journal Entry", 30), // >500 words
    BUDDHA_CONVERSATION("Buddha Chat", 15),
    BUDDHA_DEEP_CONVERSATION("Deep Conversation", 40), // >10 messages
    FUTURE_LETTER_WRITTEN("Future Letter", 25),
    FUTURE_LETTER_REFLECTED("Letter Reflection", 35),
    COMMITMENT_KEPT("Commitment Kept", 100),
    DAILY_LOGIN("Daily Login", 5),
    STREAK_BONUS("Streak Bonus", 10), // Per day of streak
    BADGE_EARNED("Badge Earned", 0), // Variable based on badge
    LEVEL_UP("Level Up", 0) // Celebration, no XP
}
