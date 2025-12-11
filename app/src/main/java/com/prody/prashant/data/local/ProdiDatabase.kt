package com.prody.prashant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prody.prashant.data.local.converter.Converters
import com.prody.prashant.data.local.dao.*
import com.prody.prashant.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VocabularyEntity::class,
        JournalEntity::class,
        FutureSelfEntity::class,
        CommitmentEntity::class,
        BuddhaConversationEntity::class,
        BuddhaMessageEntity::class,
        DailyActivityEntity::class,
        UserStatsEntity::class,
        BadgeEntity::class,
        XpTransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProdiDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun journalDao(): JournalDao
    abstract fun futureSelfDao(): FutureSelfDao
    abstract fun buddhaDao(): BuddhaDao
    abstract fun userProgressDao(): UserProgressDao

    companion object {
        private const val DATABASE_NAME = "prodi_database"

        @Volatile
        private var INSTANCE: ProdiDatabase? = null

        fun getInstance(context: Context): ProdiDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): ProdiDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ProdiDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: ProdiDatabase) {
            // Initialize user stats with default values
            database.userProgressDao().insertUserStats(UserStatsEntity())

            // Insert default badges
            database.userProgressDao().insertBadges(getDefaultBadges())

            // Insert initial vocabulary (sample philosophical content)
            database.vocabularyDao().insertAll(getInitialVocabulary())
        }

        private fun getDefaultBadges(): List<BadgeEntity> = listOf(
            // Learning Badges
            BadgeEntity(
                badgeId = "first_word",
                name = "First Word",
                description = "Learn your first vocabulary word",
                iconName = "school",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 50
            ),
            BadgeEntity(
                badgeId = "ten_words",
                name = "Word Explorer",
                description = "Learn 10 vocabulary words",
                iconName = "explore",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.BRONZE,
                requirement = 10,
                xpReward = 100
            ),
            BadgeEntity(
                badgeId = "fifty_words",
                name = "Word Scholar",
                description = "Learn 50 vocabulary words",
                iconName = "menu_book",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.SILVER,
                requirement = 50,
                xpReward = 250
            ),
            BadgeEntity(
                badgeId = "hundred_words",
                name = "Century Scholar",
                description = "Learn 100 vocabulary words",
                iconName = "military_tech",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.GOLD,
                requirement = 100,
                xpReward = 500,
                unlocksAvatar = "scholar"
            ),
            BadgeEntity(
                badgeId = "master_ten",
                name = "First Mastery",
                description = "Master 10 vocabulary words",
                iconName = "workspace_premium",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.SILVER,
                requirement = 10,
                xpReward = 200
            ),

            // Journaling Badges
            BadgeEntity(
                badgeId = "first_journal",
                name = "First Reflection",
                description = "Write your first journal entry",
                iconName = "edit_note",
                category = BadgeCategory.JOURNALING,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 50
            ),
            BadgeEntity(
                badgeId = "ten_journals",
                name = "Reflective Mind",
                description = "Write 10 journal entries",
                iconName = "psychology",
                category = BadgeCategory.JOURNALING,
                tier = BadgeTier.BRONZE,
                requirement = 10,
                xpReward = 100
            ),
            BadgeEntity(
                badgeId = "fifty_journals",
                name = "Deep Thinker",
                description = "Write 50 journal entries",
                iconName = "self_improvement",
                category = BadgeCategory.JOURNALING,
                tier = BadgeTier.SILVER,
                requirement = 50,
                xpReward = 300
            ),
            BadgeEntity(
                badgeId = "long_journal",
                name = "Soul Pourer",
                description = "Write a journal entry with 500+ words",
                iconName = "history_edu",
                category = BadgeCategory.JOURNALING,
                tier = BadgeTier.SILVER,
                requirement = 1,
                xpReward = 150
            ),
            BadgeEntity(
                badgeId = "thousand_words",
                name = "Wordsmith",
                description = "Write 1000 total journal words",
                iconName = "create",
                category = BadgeCategory.JOURNALING,
                tier = BadgeTier.BRONZE,
                requirement = 1000,
                xpReward = 100
            ),

            // Buddha Badges
            BadgeEntity(
                badgeId = "first_buddha",
                name = "Seeker",
                description = "Start your first conversation with Buddha",
                iconName = "chat",
                category = BadgeCategory.BUDDHA,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 50
            ),
            BadgeEntity(
                badgeId = "ten_buddha",
                name = "Buddha's Friend",
                description = "Have 10 conversations with Buddha",
                iconName = "forum",
                category = BadgeCategory.BUDDHA,
                tier = BadgeTier.SILVER,
                requirement = 10,
                xpReward = 200,
                unlocksAvatar = "buddha_friend"
            ),
            BadgeEntity(
                badgeId = "deep_conversation",
                name = "Deep Discourse",
                description = "Have a conversation with 20+ messages",
                iconName = "question_answer",
                category = BadgeCategory.BUDDHA,
                tier = BadgeTier.SILVER,
                requirement = 1,
                xpReward = 150
            ),

            // Streak Badges
            BadgeEntity(
                badgeId = "week_streak",
                name = "Week Warrior",
                description = "Maintain a 7-day streak",
                iconName = "local_fire_department",
                category = BadgeCategory.STREAKS,
                tier = BadgeTier.BRONZE,
                requirement = 7,
                xpReward = 100
            ),
            BadgeEntity(
                badgeId = "month_streak",
                name = "Month Master",
                description = "Maintain a 30-day streak",
                iconName = "whatshot",
                category = BadgeCategory.STREAKS,
                tier = BadgeTier.GOLD,
                requirement = 30,
                xpReward = 500,
                unlocksBanner = "flames"
            ),
            BadgeEntity(
                badgeId = "quarter_streak",
                name = "Discipline Incarnate",
                description = "Maintain a 90-day streak",
                iconName = "emoji_events",
                category = BadgeCategory.STREAKS,
                tier = BadgeTier.PLATINUM,
                requirement = 90,
                xpReward = 1000,
                unlocksAvatar = "disciplined",
                unlocksBanner = "platinum"
            ),

            // Special Badges
            BadgeEntity(
                badgeId = "early_bird",
                name = "Early Bird",
                description = "Use Prody before 6 AM",
                iconName = "wb_twilight",
                category = BadgeCategory.SPECIAL,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 75
            ),
            BadgeEntity(
                badgeId = "night_owl",
                name = "Night Owl",
                description = "Use Prody after midnight",
                iconName = "nights_stay",
                category = BadgeCategory.SPECIAL,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 75
            ),
            BadgeEntity(
                badgeId = "time_traveler",
                name = "Time Traveler",
                description = "Write your first letter to your future self",
                iconName = "schedule_send",
                category = BadgeCategory.SPECIAL,
                tier = BadgeTier.BRONZE,
                requirement = 1,
                xpReward = 100
            ),
            BadgeEntity(
                badgeId = "promise_keeper",
                name = "Promise Keeper",
                description = "Keep 5 commitments from your future self letters",
                iconName = "verified",
                category = BadgeCategory.SPECIAL,
                tier = BadgeTier.GOLD,
                requirement = 5,
                xpReward = 500,
                unlocksAvatar = "trustworthy"
            ),
            BadgeEntity(
                badgeId = "philosopher",
                name = "Philosopher",
                description = "Read 50 philosophical quotes",
                iconName = "auto_stories",
                category = BadgeCategory.LEARNING,
                tier = BadgeTier.SILVER,
                requirement = 50,
                xpReward = 250,
                unlocksAvatar = "philosopher"
            )
        )

        private fun getInitialVocabulary(): List<VocabularyEntity> = listOf(
            // Quotes from mentioned philosophers
            VocabularyEntity(
                word = "The constant assertion of belief is an indication of fear.",
                meaning = "Those who truly know do not need to constantly proclaim their beliefs; excessive assertion often masks underlying doubt and insecurity.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "Jiddu Krishnamurti"
            ),
            VocabularyEntity(
                word = "The observer is the observed.",
                meaning = "The separation between the one who perceives and what is perceived is illusory; understanding this dissolves the conflict between self and other.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "Jiddu Krishnamurti"
            ),
            VocabularyEntity(
                word = "You are never dedicated to something you have complete confidence in.",
                meaning = "True dedication often involves doubt and struggle; if something were certain, it would require no commitment.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "U.G. Krishnamurti"
            ),
            VocabularyEntity(
                word = "The demand to be free from experience is itself an experience.",
                meaning = "The very desire for liberation creates another layer of bondage; freedom cannot be sought, only recognized.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "U.G. Krishnamurti"
            ),
            VocabularyEntity(
                word = "The only way to make sense out of change is to plunge into it, move with it, and join the dance.",
                meaning = "Resisting change causes suffering; embracing life's constant flow brings peace and understanding.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "Alan Watts"
            ),
            VocabularyEntity(
                word = "Muddy water is best cleared by leaving it alone.",
                meaning = "Sometimes the best action is non-action; trying to fix everything can create more problems than it solves.",
                type = VocabularyType.QUOTE,
                category = "zen",
                author = "Alan Watts"
            ),
            VocabularyEntity(
                word = "Experience is not what happens to you, it is what you do with what happens to you.",
                meaning = "Our response to events, not the events themselves, shapes our life and character.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "Osho"
            ),
            VocabularyEntity(
                word = "Creativity is the greatest rebellion in existence.",
                meaning = "True creativity defies conformity and allows authentic self-expression beyond societal constraints.",
                type = VocabularyType.QUOTE,
                category = "philosophy",
                author = "Osho"
            ),
            VocabularyEntity(
                word = "Until you make the unconscious conscious, it will direct your life and you will call it fate.",
                meaning = "Self-awareness transforms reactive patterns into conscious choices; ignorance of our depths keeps us enslaved.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),
            VocabularyEntity(
                word = "The privilege of a lifetime is to become who you truly are.",
                meaning = "Self-realization is life's highest achievement; becoming authentic is the ultimate freedom.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),
            VocabularyEntity(
                word = "Peace comes from within. Do not seek it without.",
                meaning = "True peace cannot be found in external circumstances; it must be cultivated through inner work.",
                type = VocabularyType.QUOTE,
                category = "buddhism",
                author = "Buddha"
            ),
            VocabularyEntity(
                word = "We are what we think. All that we are arises with our thoughts.",
                meaning = "Our mental patterns create our reality; transforming thought transforms life.",
                type = VocabularyType.QUOTE,
                category = "buddhism",
                author = "Buddha"
            ),

            // Stoic Quotes
            VocabularyEntity(
                word = "We suffer more often in imagination than in reality.",
                meaning = "Most of our pain comes from anticipating or remembering suffering, not from actual present circumstances.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Seneca"
            ),
            VocabularyEntity(
                word = "You have power over your mind - not outside events. Realize this, and you will find strength.",
                meaning = "Freedom lies in mastering our responses, not in controlling external circumstances.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Marcus Aurelius"
            ),
            VocabularyEntity(
                word = "It is not things that disturb us, but our judgments about things.",
                meaning = "Our interpretations, not events themselves, create our emotional responses.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Epictetus"
            ),

            // Philosophical Words
            VocabularyEntity(
                word = "Satori",
                meaning = "A sudden moment of enlightenment or awakening in Zen Buddhism; a flash of insight into the true nature of reality.",
                example = "After years of meditation, she experienced satori while watching autumn leaves fall.",
                type = VocabularyType.WORD,
                category = "zen",
                origin = "Japanese"
            ),
            VocabularyEntity(
                word = "Ataraxia",
                meaning = "A state of serene calmness; freedom from emotional disturbance and worry.",
                example = "The stoic philosopher cultivated ataraxia through daily practice.",
                type = VocabularyType.WORD,
                category = "philosophy",
                origin = "Greek"
            ),
            VocabularyEntity(
                word = "Memento Mori",
                meaning = "Remember that you will die; a meditation on mortality used to appreciate life and maintain perspective.",
                example = "The ancient skull on his desk served as memento mori.",
                type = VocabularyType.PHRASE,
                category = "stoicism",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Amor Fati",
                meaning = "Love of fate; embracing everything that happens in life, including suffering, as necessary and beneficial.",
                example = "With amor fati, she accepted both triumphs and failures equally.",
                type = VocabularyType.PHRASE,
                category = "stoicism",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Dharma",
                meaning = "The cosmic law underlying right behavior and social order; one's duty or purpose in life.",
                example = "He dedicated his life to following his dharma as a teacher.",
                type = VocabularyType.WORD,
                category = "vedic",
                origin = "Sanskrit"
            ),
            VocabularyEntity(
                word = "Maya",
                meaning = "The illusion of the material world hiding the underlying unity of existence; the veil of perception.",
                example = "The sage saw through maya to perceive the interconnectedness of all things.",
                type = VocabularyType.WORD,
                category = "vedic",
                origin = "Sanskrit"
            ),
            VocabularyEntity(
                word = "Wu Wei",
                meaning = "Non-action or effortless action; acting in accordance with the natural flow of life without forcing.",
                example = "The master gardener practiced wu wei, working with nature rather than against it.",
                type = VocabularyType.PHRASE,
                category = "zen",
                origin = "Chinese"
            ),

            // Proverbs
            VocabularyEntity(
                word = "The best time to plant a tree was twenty years ago. The second best time is now.",
                meaning = "While the past cannot be changed, the present moment always offers opportunity for action and growth.",
                type = VocabularyType.PROVERB,
                category = "wisdom",
                author = "Chinese Proverb"
            ),
            VocabularyEntity(
                word = "Fall seven times, stand up eight.",
                meaning = "Resilience and persistence are more important than never failing; success comes from continuing despite setbacks.",
                type = VocabularyType.PROVERB,
                category = "wisdom",
                author = "Japanese Proverb"
            ),
            VocabularyEntity(
                word = "The mind is everything. What you think you become.",
                meaning = "Our thoughts shape our reality and determine who we become; mental discipline is life's foundation.",
                type = VocabularyType.PROVERB,
                category = "wisdom",
                author = "Ancient Wisdom"
            ),

            // Idioms
            VocabularyEntity(
                word = "Burning the midnight oil",
                meaning = "Working or studying late into the night.",
                example = "She was burning the midnight oil to finish her philosophy thesis.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),
            VocabularyEntity(
                word = "A penny for your thoughts",
                meaning = "A way of asking someone what they are thinking about.",
                example = "You've been quiet all evening - a penny for your thoughts?",
                type = VocabularyType.IDIOM,
                category = "general"
            )
        )
    }
}
