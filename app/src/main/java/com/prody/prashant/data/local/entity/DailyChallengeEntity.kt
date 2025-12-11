package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a daily challenge that encourages user engagement and growth.
 * Challenges are generated each day based on user activity and preferences.
 */
@Entity(
    tableName = "daily_challenges",
    indices = [
        Index(value = ["date"]),
        Index(value = ["type"]),
        Index(value = ["isCompleted"])
    ]
)
@Serializable
data class DailyChallengeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: Long, // Start of day timestamp

    val type: ChallengeType,
    val title: String,
    val description: String,
    val requirement: Int, // What needs to be achieved (e.g., 3 words, 1 journal entry)
    val progress: Int = 0,
    val xpReward: Int,

    val isCompleted: Boolean = false,
    val completedAt: Long? = null,

    val philosopherQuote: String? = null, // Inspirational quote for the challenge
    val quoteAuthor: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Types of daily challenges available.
 */
@Serializable
enum class ChallengeType(
    val displayName: String,
    val iconName: String,
    val description: String
) {
    LEARN_WORDS(
        displayName = "Word Explorer",
        iconName = "school",
        description = "Learn new vocabulary words"
    ),
    REVIEW_WORDS(
        displayName = "Review Master",
        iconName = "refresh",
        description = "Review vocabulary you've learned"
    ),
    WRITE_JOURNAL(
        displayName = "Reflective Writing",
        iconName = "edit_note",
        description = "Write a journal entry"
    ),
    LONG_JOURNAL(
        displayName = "Deep Reflection",
        iconName = "history_edu",
        description = "Write a detailed journal entry"
    ),
    BUDDHA_CHAT(
        displayName = "Seek Wisdom",
        iconName = "chat",
        description = "Have a conversation with Buddha"
    ),
    DEEP_CONVERSATION(
        displayName = "Deep Dialogue",
        iconName = "forum",
        description = "Have an extended conversation with Buddha"
    ),
    FUTURE_LETTER(
        displayName = "Time Capsule",
        iconName = "schedule_send",
        description = "Write to your future self"
    ),
    QUOTE_REFLECTION(
        displayName = "Quote Contemplation",
        iconName = "format_quote",
        description = "Read and reflect on philosophical quotes"
    ),
    STREAK_MAINTAIN(
        displayName = "Consistency Check",
        iconName = "local_fire_department",
        description = "Maintain your learning streak"
    ),
    MIXED_ACTIVITY(
        displayName = "Well-Rounded",
        iconName = "widgets",
        description = "Engage in multiple activities"
    ),
    EARLY_BIRD(
        displayName = "Early Bird",
        iconName = "wb_twilight",
        description = "Complete activities before 9 AM"
    ),
    NIGHT_OWL(
        displayName = "Night Owl",
        iconName = "nights_stay",
        description = "Engage in mindful reflection after dark"
    )
}

/**
 * Generates appropriate daily challenges based on user context.
 */
object DailyChallengeGenerator {

    private val philosopherQuotes = mapOf(
        ChallengeType.LEARN_WORDS to listOf(
            "The limits of my language mean the limits of my world." to "Ludwig Wittgenstein",
            "A different language is a different vision of life." to "Federico Fellini",
            "Language is the house of Being." to "Martin Heidegger"
        ),
        ChallengeType.REVIEW_WORDS to listOf(
            "Repetition is the mother of learning." to "Ancient Proverb",
            "What we learn with pleasure we never forget." to "Alfred Mercier",
            "The more you practice, the more effortless it becomes." to "Zen Wisdom"
        ),
        ChallengeType.WRITE_JOURNAL to listOf(
            "Know thyself." to "Socrates",
            "The unexamined life is not worth living." to "Socrates",
            "Writing is thinking on paper." to "William Zinsser"
        ),
        ChallengeType.LONG_JOURNAL to listOf(
            "In the depth of winter, I found there was within me an invincible summer." to "Albert Camus",
            "The soul becomes dyed with the color of its thoughts." to "Marcus Aurelius",
            "We write to taste life twice." to "Anaïs Nin"
        ),
        ChallengeType.BUDDHA_CHAT to listOf(
            "The only true wisdom is in knowing you know nothing." to "Socrates",
            "Seeking wisdom is a voyage, not a harbor." to "Rumi",
            "Questions are the beginning of wisdom." to "Buddhist Teaching"
        ),
        ChallengeType.DEEP_CONVERSATION to listOf(
            "Conversation enriches the understanding, but solitude is the school of genius." to "Edward Gibbon",
            "Let your dialogue be such as to leave a good impression." to "Epictetus",
            "Real dialogue happens when we dare to be vulnerable." to "Brené Brown"
        ),
        ChallengeType.FUTURE_LETTER to listOf(
            "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese Proverb",
            "Your future self is watching you right now through your memories." to "Aubrey de Grey",
            "Today's actions are tomorrow's memories." to "Zen Teaching"
        ),
        ChallengeType.QUOTE_REFLECTION to listOf(
            "Words can inspire and words can destroy. Choose yours well." to "Robin Sharma",
            "Great thoughts speak only to the thoughtful mind." to "Thoreau",
            "In the silence between thoughts, wisdom is born." to "Eckhart Tolle"
        ),
        ChallengeType.STREAK_MAINTAIN to listOf(
            "We are what we repeatedly do. Excellence is not an act, but a habit." to "Aristotle",
            "Small daily improvements are the key to staggering long-term results." to "Unknown",
            "Discipline is the bridge between goals and accomplishment." to "Jim Rohn"
        ),
        ChallengeType.MIXED_ACTIVITY to listOf(
            "A well-balanced life brings joy and harmony." to "Ancient Wisdom",
            "Balance is not something you find, it's something you create." to "Jana Kingsford",
            "In all things, balance." to "Stoic Teaching"
        ),
        ChallengeType.EARLY_BIRD to listOf(
            "The early morning has gold in its mouth." to "Benjamin Franklin",
            "Each morning we are born again." to "Buddha",
            "Morning is when I am awake and there is a dawn in me." to "Thoreau"
        ),
        ChallengeType.NIGHT_OWL to listOf(
            "The night is more alive and more richly colored than the day." to "Van Gogh",
            "In the stillness of the quiet, you hear the truth." to "Unknown",
            "Stars can't shine without darkness." to "Unknown"
        )
    )

    /**
     * Generates challenges for a new day based on user patterns.
     */
    fun generateDailyChallenges(
        date: Long,
        currentStreak: Int,
        wordsLearned: Int,
        journalCount: Int,
        buddhaConversations: Int,
        futureLetters: Int,
        hourOfDay: Int = 12
    ): List<DailyChallengeEntity> {
        val challenges = mutableListOf<DailyChallengeEntity>()

        // Primary challenge based on lowest engagement area
        val primaryChallenge = when {
            wordsLearned < 10 -> createChallenge(date, ChallengeType.LEARN_WORDS, 3, 25)
            journalCount < 5 -> createChallenge(date, ChallengeType.WRITE_JOURNAL, 1, 30)
            buddhaConversations < 3 -> createChallenge(date, ChallengeType.BUDDHA_CHAT, 1, 25)
            else -> createChallenge(date, ChallengeType.LEARN_WORDS, 5, 35)
        }
        challenges.add(primaryChallenge)

        // Streak-based challenge
        if (currentStreak > 0) {
            challenges.add(createChallenge(
                date = date,
                type = ChallengeType.STREAK_MAINTAIN,
                requirement = 1,
                xpReward = 15 + (currentStreak * 2).coerceAtMost(30)
            ))
        }

        // Time-based challenge
        val timeChallenge = when {
            hourOfDay < 9 -> createChallenge(date, ChallengeType.EARLY_BIRD, 1, 20)
            hourOfDay >= 21 -> createChallenge(date, ChallengeType.NIGHT_OWL, 1, 20)
            else -> null
        }
        timeChallenge?.let { challenges.add(it) }

        // Bonus challenge for variety
        val bonusChallenge = when {
            wordsLearned >= 20 && journalCount < 10 -> createChallenge(date, ChallengeType.LONG_JOURNAL, 1, 40)
            buddhaConversations >= 5 -> createChallenge(date, ChallengeType.DEEP_CONVERSATION, 10, 35)
            futureLetters < 1 -> createChallenge(date, ChallengeType.FUTURE_LETTER, 1, 30)
            else -> createChallenge(date, ChallengeType.QUOTE_REFLECTION, 5, 20)
        }
        challenges.add(bonusChallenge)

        return challenges.take(3) // Return max 3 challenges per day
    }

    private fun createChallenge(
        date: Long,
        type: ChallengeType,
        requirement: Int,
        xpReward: Int
    ): DailyChallengeEntity {
        val quotes = philosopherQuotes[type] ?: emptyList()
        val (quote, author) = if (quotes.isNotEmpty()) {
            quotes.random()
        } else {
            null to null
        }

        val title = when (type) {
            ChallengeType.LEARN_WORDS -> "Learn $requirement New Words"
            ChallengeType.REVIEW_WORDS -> "Review $requirement Words"
            ChallengeType.WRITE_JOURNAL -> if (requirement == 1) "Write a Journal Entry" else "Write $requirement Journal Entries"
            ChallengeType.LONG_JOURNAL -> "Write a Deep Reflection (200+ words)"
            ChallengeType.BUDDHA_CHAT -> "Start a Conversation with Buddha"
            ChallengeType.DEEP_CONVERSATION -> "Have a Meaningful Dialogue ($requirement+ messages)"
            ChallengeType.FUTURE_LETTER -> "Write to Your Future Self"
            ChallengeType.QUOTE_REFLECTION -> "Contemplate $requirement Philosophical Quotes"
            ChallengeType.STREAK_MAINTAIN -> "Keep Your Streak Alive"
            ChallengeType.MIXED_ACTIVITY -> "Complete $requirement Different Activities"
            ChallengeType.EARLY_BIRD -> "Complete an Activity Before 9 AM"
            ChallengeType.NIGHT_OWL -> "Reflect After Sunset"
        }

        return DailyChallengeEntity(
            date = date,
            type = type,
            title = title,
            description = type.description,
            requirement = requirement,
            xpReward = xpReward,
            philosopherQuote = quote,
            quoteAuthor = author
        )
    }
}
