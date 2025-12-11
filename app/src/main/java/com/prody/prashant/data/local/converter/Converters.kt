package com.prody.prashant.data.local.converter

import androidx.room.TypeConverter
import com.prody.prashant.data.local.entity.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room type converters for complex types.
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // VocabularyType
    @TypeConverter
    fun fromVocabularyType(value: VocabularyType): String = value.name

    @TypeConverter
    fun toVocabularyType(value: String): VocabularyType = VocabularyType.valueOf(value)

    // LearningStatus
    @TypeConverter
    fun fromLearningStatus(value: LearningStatus): String = value.name

    @TypeConverter
    fun toLearningStatus(value: String): LearningStatus = LearningStatus.valueOf(value)

    // Mood
    @TypeConverter
    fun fromMood(value: Mood): String = value.name

    @TypeConverter
    fun toMood(value: String): Mood = Mood.valueOf(value)

    // TimeOfDay
    @TypeConverter
    fun fromTimeOfDay(value: TimeOfDay): String = value.name

    @TypeConverter
    fun toTimeOfDay(value: String): TimeOfDay = TimeOfDay.valueOf(value)

    // MessageRole
    @TypeConverter
    fun fromMessageRole(value: MessageRole): String = value.name

    @TypeConverter
    fun toMessageRole(value: String): MessageRole = MessageRole.valueOf(value)

    // CommitmentStatus
    @TypeConverter
    fun fromCommitmentStatus(value: CommitmentStatus): String = value.name

    @TypeConverter
    fun toCommitmentStatus(value: String): CommitmentStatus = CommitmentStatus.valueOf(value)

    // LevelTitle
    @TypeConverter
    fun fromLevelTitle(value: LevelTitle): String = value.name

    @TypeConverter
    fun toLevelTitle(value: String): LevelTitle = LevelTitle.valueOf(value)

    // BadgeCategory
    @TypeConverter
    fun fromBadgeCategory(value: BadgeCategory): String = value.name

    @TypeConverter
    fun toBadgeCategory(value: String): BadgeCategory = BadgeCategory.valueOf(value)

    // BadgeTier
    @TypeConverter
    fun fromBadgeTier(value: BadgeTier): String = value.name

    @TypeConverter
    fun toBadgeTier(value: String): BadgeTier = BadgeTier.valueOf(value)

    // XpSource
    @TypeConverter
    fun fromXpSource(value: XpSource): String = value.name

    @TypeConverter
    fun toXpSource(value: String): XpSource = XpSource.valueOf(value)

    // List<String> - for comma-separated lists
    @TypeConverter
    fun fromStringList(value: List<String>?): String? = value?.joinToString(",")

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.takeIf { it.isNotBlank() }?.split(",")?.map { it.trim() }

    // BuddhaMode
    @TypeConverter
    fun fromBuddhaMode(value: BuddhaMode): String = value.name

    @TypeConverter
    fun toBuddhaMode(value: String): BuddhaMode = BuddhaMode.valueOf(value)
}
