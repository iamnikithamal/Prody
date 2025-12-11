package com.prody.prashant.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the Prodi app.
 * Uses type-safe navigation with Kotlin Serialization.
 */

// ==================== Bottom Navigation Routes ====================

sealed interface BottomNavRoute {
    val route: String
    val label: String
    val selectedIcon: ImageVector
    val unselectedIcon: ImageVector
}

@Serializable
object HomeRoute : BottomNavRoute {
    override val route = "home"
    override val label = "Home"
    override val selectedIcon = Icons.Filled.Home
    override val unselectedIcon = Icons.Outlined.Home
}

@Serializable
object LearnRoute : BottomNavRoute {
    override val route = "learn"
    override val label = "Learn"
    override val selectedIcon = Icons.Filled.School
    override val unselectedIcon = Icons.Outlined.School
}

@Serializable
object JournalRoute : BottomNavRoute {
    override val route = "journal"
    override val label = "Journal"
    override val selectedIcon = Icons.Filled.Book
    override val unselectedIcon = Icons.Outlined.Book
}

@Serializable
object BuddhaRoute : BottomNavRoute {
    override val route = "buddha"
    override val label = "Buddha"
    override val selectedIcon = Icons.Filled.SelfImprovement
    override val unselectedIcon = Icons.Outlined.SelfImprovement
}

@Serializable
object StatsRoute : BottomNavRoute {
    override val route = "stats"
    override val label = "Stats"
    override val selectedIcon = Icons.Filled.BarChart
    override val unselectedIcon = Icons.Outlined.BarChart
}

val bottomNavItems = listOf(HomeRoute, LearnRoute, JournalRoute, BuddhaRoute, StatsRoute)

// ==================== Screen Routes ====================

@Serializable
object SplashRoute

@Serializable
object OnboardingRoute

@Serializable
object MainRoute

// ==================== Learn Sub-Routes ====================

@Serializable
data class VocabularyDetailRoute(val id: Long)

@Serializable
data class VocabularyListRoute(val type: String) // "word", "quote", "proverb", "idiom", "phrase"

@Serializable
object VocabularyPracticeRoute

@Serializable
object VocabularySearchRoute

// ==================== Journal Sub-Routes ====================

@Serializable
object JournalNewRoute

@Serializable
data class JournalDetailRoute(val id: Long)

@Serializable
data class JournalEditRoute(val id: Long)

// ==================== Buddha Sub-Routes ====================

@Serializable
data class BuddhaConversationRoute(val conversationId: Long? = null)

@Serializable
object BuddhaHistoryRoute

// ==================== Future Self Sub-Routes ====================

@Serializable
object FutureSelfRoute

@Serializable
object FutureSelfNewRoute

@Serializable
data class FutureSelfDetailRoute(val id: Long)

// ==================== Profile & Settings Routes ====================

@Serializable
object ProfileRoute

@Serializable
object ProfileEditRoute

@Serializable
object SettingsRoute

@Serializable
object SettingsAppearanceRoute

@Serializable
object SettingsNotificationsRoute

@Serializable
object SettingsAiRoute

@Serializable
object SettingsDataRoute

@Serializable
object SettingsAboutRoute

// ==================== Stats Sub-Routes ====================

@Serializable
object BadgesRoute

@Serializable
object LeaderboardRoute

@Serializable
object ActivityCalendarRoute

// ==================== Navigation Helper ====================

object ProdiDestinations {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val MAIN = "main"
    const val HOME = "home"
    const val LEARN = "learn"
    const val JOURNAL = "journal"
    const val BUDDHA = "buddha"
    const val STATS = "stats"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val FUTURE_SELF = "future_self"

    // Detail routes with arguments
    const val VOCABULARY_DETAIL = "vocabulary/{id}"
    const val VOCABULARY_LIST = "vocabulary_list/{type}"
    const val VOCABULARY_PRACTICE = "vocabulary_practice"
    const val VOCABULARY_SEARCH = "vocabulary_search"

    const val JOURNAL_NEW = "journal_new"
    const val JOURNAL_DETAIL = "journal/{id}"
    const val JOURNAL_EDIT = "journal_edit/{id}"

    const val BUDDHA_CONVERSATION = "buddha_conversation"
    const val BUDDHA_HISTORY = "buddha_history"

    const val FUTURE_SELF_NEW = "future_self_new"
    const val FUTURE_SELF_DETAIL = "future_self/{id}"

    const val PROFILE_EDIT = "profile_edit"

    const val SETTINGS_APPEARANCE = "settings_appearance"
    const val SETTINGS_NOTIFICATIONS = "settings_notifications"
    const val SETTINGS_AI = "settings_ai"
    const val SETTINGS_DATA = "settings_data"
    const val SETTINGS_ABOUT = "settings_about"

    const val BADGES = "badges"
    const val LEADERBOARD = "leaderboard"
    const val ACTIVITY_CALENDAR = "activity_calendar"
}
