package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a journal entry where users can express their thoughts.
 * Buddha AI analyzes and provides insights on entries.
 */
@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["mood"]),
        Index(value = ["isAnalyzed"]),
        Index(value = ["isFavorite"])
    ]
)
@Serializable
data class JournalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String? = null,
    val content: String,
    val mood: Mood = Mood.NEUTRAL,
    val tags: String? = null, // Comma-separated tags

    // Buddha AI Analysis
    val isAnalyzed: Boolean = false,
    val aiSummary: String? = null,
    val aiReflection: String? = null, // Philosophical reflection from Buddha
    val aiSuggestion: String? = null, // Actionable suggestion
    val aiSentiment: String? = null, // Overall sentiment analysis
    val aiKeyThemes: String? = null, // Comma-separated key themes identified

    val isFavorite: Boolean = false,
    val wordCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // For tracking writing patterns
    val writingDurationSeconds: Int? = null,
    val timeOfDay: TimeOfDay = TimeOfDay.fromHour(
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    )
)

@Serializable
enum class Mood(val emoji: String, val displayName: String) {
    HAPPY("\uD83D\uDE0A", "Happy"),
    CALM("\uD83D\uDE0C", "Calm"),
    GRATEFUL("\uD83D\uDE4F", "Grateful"),
    ANXIOUS("\uD83D\uDE1F", "Anxious"),
    SAD("\uD83D\uDE22", "Sad"),
    ANGRY("\uD83D\uDE20", "Angry"),
    CONFUSED("\uD83E\uDD14", "Confused"),
    MOTIVATED("\uD83D\uDCAA", "Motivated"),
    TIRED("\uD83D\uDE29", "Tired"),
    HOPEFUL("\u2728", "Hopeful"),
    REFLECTIVE("\uD83E\uDDD8", "Reflective"),
    NEUTRAL("\uD83D\uDE10", "Neutral")
}

@Serializable
enum class TimeOfDay {
    EARLY_MORNING, // 5-8
    MORNING, // 8-12
    AFTERNOON, // 12-17
    EVENING, // 17-21
    NIGHT; // 21-5

    companion object {
        fun fromHour(hour: Int): TimeOfDay = when (hour) {
            in 5..7 -> EARLY_MORNING
            in 8..11 -> MORNING
            in 12..16 -> AFTERNOON
            in 17..20 -> EVENING
            else -> NIGHT
        }
    }
}
