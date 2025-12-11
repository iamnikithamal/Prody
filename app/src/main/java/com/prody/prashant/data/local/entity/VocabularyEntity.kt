package com.prody.prashant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a vocabulary item (word, proverb, idiom, phrase, or quote).
 * Uses spaced repetition algorithm for effective learning.
 */
@Entity(
    tableName = "vocabulary",
    indices = [
        Index(value = ["word"], unique = true),
        Index(value = ["type"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["learningStatus"]),
        Index(value = ["nextReviewDate"])
    ]
)
@Serializable
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val word: String,
    val meaning: String,
    val example: String? = null,
    val pronunciation: String? = null,
    val origin: String? = null,
    val synonyms: String? = null, // Comma-separated list
    val antonyms: String? = null, // Comma-separated list
    val partOfSpeech: String? = null, // noun, verb, adjective, etc.

    val type: VocabularyType = VocabularyType.WORD,
    val category: String = "general",
    val author: String? = null, // For quotes

    val isFavorite: Boolean = false,
    val learningStatus: LearningStatus = LearningStatus.NEW,

    // Spaced Repetition System (SRS) fields
    val reviewCount: Int = 0,
    val correctCount: Int = 0,
    val easeFactor: Float = 2.5f, // SM-2 algorithm ease factor
    val intervalDays: Int = 1,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val lastReviewDate: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // AI-generated context
    val aiMnemonic: String? = null,
    val aiUsageNotes: String? = null
)

@Serializable
enum class VocabularyType {
    WORD,
    PROVERB,
    IDIOM,
    PHRASE,
    QUOTE
}

@Serializable
enum class LearningStatus {
    NEW,
    LEARNING,
    REVIEWING,
    MASTERED
}
