package com.prody.prashant.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prody.prashant.data.local.converter.Converters
import com.prody.prashant.data.local.dao.BuddhaDao
import com.prody.prashant.data.local.dao.DailyChallengeDao
import com.prody.prashant.data.local.dao.FutureSelfDao
import com.prody.prashant.data.local.dao.JournalDao
import com.prody.prashant.data.local.dao.UserProgressDao
import com.prody.prashant.data.local.dao.VocabularyDao
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
        XpTransactionEntity::class,
        DailyChallengeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ProdiDatabase : RoomDatabase() {

    abstract fun vocabularyDao(): VocabularyDao
    abstract fun journalDao(): JournalDao
    abstract fun futureSelfDao(): FutureSelfDao
    abstract fun buddhaDao(): BuddhaDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun dailyChallengeDao(): DailyChallengeDao

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
            ),

            // Additional Philosophical Words
            VocabularyEntity(
                word = "Eudaimonia",
                meaning = "Human flourishing or well-being; living in accordance with virtue and realizing one's full potential.",
                example = "For Aristotle, eudaimonia was the highest human good, achieved through virtuous living.",
                type = VocabularyType.WORD,
                category = "philosophy",
                origin = "Greek"
            ),
            VocabularyEntity(
                word = "Kairos",
                meaning = "The opportune or decisive moment; the right time for action distinct from chronological time.",
                example = "She recognized the kairos and seized the opportunity that would change her life.",
                type = VocabularyType.WORD,
                category = "philosophy",
                origin = "Greek"
            ),
            VocabularyEntity(
                word = "Praxis",
                meaning = "Practical action informed by theory; the process of putting ideas into practice.",
                example = "True wisdom is not just knowledge but praxis - putting understanding into action.",
                type = VocabularyType.WORD,
                category = "philosophy",
                origin = "Greek"
            ),
            VocabularyEntity(
                word = "Sonder",
                meaning = "The realization that each passerby has a life as vivid and complex as your own.",
                example = "Looking at the crowded street, he felt sonder wash over him - each person carrying their own universe.",
                type = VocabularyType.WORD,
                category = "existential",
                origin = "Modern"
            ),
            VocabularyEntity(
                word = "Kintsugi",
                meaning = "The Japanese art of repairing broken pottery with gold; embracing flaws and imperfections as part of history.",
                example = "She saw her failures through the lens of kintsugi - they made her stronger, not weaker.",
                type = VocabularyType.WORD,
                category = "zen",
                origin = "Japanese"
            ),
            VocabularyEntity(
                word = "Arete",
                meaning = "Excellence or virtue; fulfilling one's purpose and being the best version of oneself.",
                example = "The athlete pursued arete in every aspect of training, not just physical but mental and moral.",
                type = VocabularyType.WORD,
                category = "philosophy",
                origin = "Greek"
            ),
            VocabularyEntity(
                word = "Samadhi",
                meaning = "A state of intense concentration achieved through meditation; union of subject and object.",
                example = "Through years of practice, the monk finally experienced samadhi during his meditation.",
                type = VocabularyType.WORD,
                category = "vedic",
                origin = "Sanskrit"
            ),
            VocabularyEntity(
                word = "Ikigai",
                meaning = "A Japanese concept meaning 'reason for being'; what gives life meaning and purpose.",
                example = "After retirement, she found her ikigai in teaching philosophy to young people.",
                type = VocabularyType.WORD,
                category = "zen",
                origin = "Japanese"
            ),

            // More Stoic Quotes
            VocabularyEntity(
                word = "The impediment to action advances action. What stands in the way becomes the way.",
                meaning = "Obstacles are opportunities; the very things that block us can become the means of our progress.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Marcus Aurelius"
            ),
            VocabularyEntity(
                word = "Waste no more time arguing about what a good man should be. Be one.",
                meaning = "Philosophy without action is empty; true understanding manifests in how we live, not what we debate.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Marcus Aurelius"
            ),
            VocabularyEntity(
                word = "It is not that we have a short time to live, but that we waste a lot of it.",
                meaning = "Life is long enough if well-used; our sense of time scarcity comes from squandering our hours.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Seneca"
            ),
            VocabularyEntity(
                word = "He who fears death will never do anything worthy of a living man.",
                meaning = "Fear of death paralyzes us; accepting mortality frees us to live fully and courageously.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Seneca"
            ),
            VocabularyEntity(
                word = "Don't explain your philosophy. Embody it.",
                meaning = "Actions speak louder than words; the truest expression of wisdom is how we live each moment.",
                type = VocabularyType.QUOTE,
                category = "stoicism",
                author = "Epictetus"
            ),

            // Carl Jung Quotes
            VocabularyEntity(
                word = "I am not what happened to me, I am what I choose to become.",
                meaning = "Our past shapes us but doesn't determine us; we have the power to define who we become.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),
            VocabularyEntity(
                word = "Who looks outside, dreams; who looks inside, awakes.",
                meaning = "External seeking leads to fantasy; true awakening comes from inner exploration and self-knowledge.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),
            VocabularyEntity(
                word = "Everything that irritates us about others can lead us to an understanding of ourselves.",
                meaning = "Our reactions to others mirror our own unconscious; irritation reveals our shadow projections.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),
            VocabularyEntity(
                word = "The meeting of two personalities is like the contact of two chemical substances: if there is any reaction, both are transformed.",
                meaning = "Genuine encounters change both parties; every real meeting is an opportunity for mutual transformation.",
                type = VocabularyType.QUOTE,
                category = "psychology",
                author = "Carl Jung"
            ),

            // Buddhist Wisdom
            VocabularyEntity(
                word = "In the end, only three things matter: how much you loved, how gently you lived, and how gracefully you let go.",
                meaning = "Life's meaning lies not in accumulation but in love, kindness, and the wisdom of non-attachment.",
                type = VocabularyType.QUOTE,
                category = "buddhism",
                author = "Buddha"
            ),
            VocabularyEntity(
                word = "You yourself, as much as anybody in the entire universe, deserve your love and affection.",
                meaning = "Self-compassion is not selfishness; caring for yourself is the foundation of caring for others.",
                type = VocabularyType.QUOTE,
                category = "buddhism",
                author = "Buddha"
            ),
            VocabularyEntity(
                word = "Every morning we are born again. What we do today is what matters most.",
                meaning = "Each day offers fresh beginning; the past is gone, and this moment is where life unfolds.",
                type = VocabularyType.QUOTE,
                category = "buddhism",
                author = "Buddha"
            ),

            // More Proverbs
            VocabularyEntity(
                word = "A smooth sea never made a skilled sailor.",
                meaning = "Challenges and difficulties are essential for growth; comfort zones limit our development.",
                type = VocabularyType.PROVERB,
                category = "wisdom",
                author = "English Proverb"
            ),
            VocabularyEntity(
                word = "When the winds of change blow, some build walls while others build windmills.",
                meaning = "Change can be a threat or opportunity depending on our response; wisdom lies in adaptation.",
                type = VocabularyType.PROVERB,
                category = "wisdom",
                author = "Chinese Proverb"
            ),
            VocabularyEntity(
                word = "The wound is the place where the light enters you.",
                meaning = "Our deepest pain can become our greatest source of wisdom and compassion.",
                type = VocabularyType.QUOTE,
                category = "sufism",
                author = "Rumi"
            ),
            VocabularyEntity(
                word = "What you seek is seeking you.",
                meaning = "Our deepest desires reflect something in the universe calling to us; longing is mutual.",
                type = VocabularyType.QUOTE,
                category = "sufism",
                author = "Rumi"
            ),
            VocabularyEntity(
                word = "Yesterday I was clever, so I wanted to change the world. Today I am wise, so I am changing myself.",
                meaning = "True change begins within; maturity recognizes we can only transform the world by transforming ourselves.",
                type = VocabularyType.QUOTE,
                category = "sufism",
                author = "Rumi"
            ),

            // More Idioms
            VocabularyEntity(
                word = "Break a leg",
                meaning = "A way to wish someone good luck, especially before a performance.",
                example = "Before her speech, her mentor said 'Break a leg!' with an encouraging smile.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),
            VocabularyEntity(
                word = "Every cloud has a silver lining",
                meaning = "Even difficult situations contain some positive aspects or opportunities.",
                example = "Losing that job was devastating, but every cloud has a silver lining - it led me to my true calling.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),
            VocabularyEntity(
                word = "The ball is in your court",
                meaning = "It is your decision or responsibility to take the next action.",
                example = "I've made my position clear; now the ball is in your court.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),
            VocabularyEntity(
                word = "Bite the bullet",
                meaning = "To endure a painful or difficult situation with courage.",
                example = "Sometimes you just have to bite the bullet and have that difficult conversation.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),
            VocabularyEntity(
                word = "Let the cat out of the bag",
                meaning = "To accidentally reveal a secret.",
                example = "She let the cat out of the bag about the surprise party.",
                type = VocabularyType.IDIOM,
                category = "general"
            ),

            // Latin Phrases
            VocabularyEntity(
                word = "Carpe Diem",
                meaning = "Seize the day; make the most of present opportunities without excessive worry about the future.",
                example = "With carpe diem as his motto, he never passed up a chance to learn something new.",
                type = VocabularyType.PHRASE,
                category = "philosophy",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Cogito Ergo Sum",
                meaning = "I think, therefore I am; the foundation of modern philosophy asserting that thinking proves existence.",
                example = "Descartes' cogito ergo sum remains a cornerstone of philosophical inquiry.",
                type = VocabularyType.PHRASE,
                category = "philosophy",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Tempus Fugit",
                meaning = "Time flies; a reminder of time's swift passage and the importance of using it wisely.",
                example = "Tempus fugit - before she knew it, twenty years had passed since graduation.",
                type = VocabularyType.PHRASE,
                category = "philosophy",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Per Aspera Ad Astra",
                meaning = "Through hardships to the stars; achievement comes through overcoming difficulties.",
                example = "Per aspera ad astra - her journey from poverty to success embodied this ancient wisdom.",
                type = VocabularyType.PHRASE,
                category = "wisdom",
                origin = "Latin"
            ),
            VocabularyEntity(
                word = "Tabula Rasa",
                meaning = "Blank slate; the idea that we begin life without preformed mental content.",
                example = "Each new year offers us a tabula rasa, a fresh start to become who we want to be.",
                type = VocabularyType.PHRASE,
                category = "philosophy",
                origin = "Latin"
            )
        )
    }
}
