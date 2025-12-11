package com.prody.prashant.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.prody.prashant.ui.screens.buddha.BuddhaConversationScreen
import com.prody.prashant.ui.screens.buddha.BuddhaHistoryScreen
import com.prody.prashant.ui.screens.buddha.BuddhaScreen
import com.prody.prashant.ui.screens.futureme.FutureSelfDetailScreen
import com.prody.prashant.ui.screens.futureme.FutureSelfNewScreen
import com.prody.prashant.ui.screens.futureme.FutureSelfScreen
import com.prody.prashant.ui.screens.home.HomeScreen
import com.prody.prashant.ui.screens.journal.JournalDetailScreen
import com.prody.prashant.ui.screens.journal.JournalEditScreen
import com.prody.prashant.ui.screens.journal.JournalNewScreen
import com.prody.prashant.ui.screens.journal.JournalScreen
import com.prody.prashant.ui.screens.learn.LearnScreen
import com.prody.prashant.ui.screens.learn.VocabularyDetailScreen
import com.prody.prashant.ui.screens.learn.VocabularyListScreen
import com.prody.prashant.ui.screens.learn.VocabularyPracticeScreen
import com.prody.prashant.ui.screens.learn.VocabularySearchScreen
import com.prody.prashant.ui.screens.profile.ProfileEditScreen
import com.prody.prashant.ui.screens.profile.ProfileScreen
import com.prody.prashant.ui.screens.settings.*
import com.prody.prashant.ui.screens.stats.ActivityCalendarScreen
import com.prody.prashant.ui.screens.stats.BadgesScreen
import com.prody.prashant.ui.screens.stats.LeaderboardScreen
import com.prody.prashant.ui.screens.stats.StatsScreen

private const val ANIM_DURATION = 300

/**
 * Main navigation host for the app's authenticated screens.
 */
@Composable
fun ProdiNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = ProdiDestinations.HOME,
        modifier = modifier.padding(innerPadding),
        enterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(ANIM_DURATION))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(ANIM_DURATION))
        }
    ) {
        // ==================== Bottom Nav Destinations ====================

        composable(ProdiDestinations.HOME) {
            HomeScreen(
                onNavigateToLearn = { navController.navigate(ProdiDestinations.LEARN) },
                onNavigateToJournal = { navController.navigate(ProdiDestinations.JOURNAL) },
                onNavigateToBuddha = { navController.navigate(ProdiDestinations.BUDDHA) },
                onNavigateToFutureSelf = { navController.navigate(ProdiDestinations.FUTURE_SELF) },
                onNavigateToProfile = { navController.navigate(ProdiDestinations.PROFILE) },
                onNavigateToSettings = { navController.navigate(ProdiDestinations.SETTINGS) },
                onNavigateToVocabularyDetail = { id ->
                    navController.navigate("vocabulary/$id")
                }
            )
        }

        composable(ProdiDestinations.LEARN) {
            LearnScreen(
                onNavigateToVocabularyList = { type ->
                    navController.navigate("vocabulary_list/$type")
                },
                onNavigateToVocabularyDetail = { id ->
                    navController.navigate("vocabulary/$id")
                },
                onNavigateToPractice = {
                    navController.navigate(ProdiDestinations.VOCABULARY_PRACTICE)
                },
                onNavigateToSearch = {
                    navController.navigate(ProdiDestinations.VOCABULARY_SEARCH)
                }
            )
        }

        composable(ProdiDestinations.JOURNAL) {
            JournalScreen(
                onNavigateToNewEntry = {
                    navController.navigate(ProdiDestinations.JOURNAL_NEW)
                },
                onNavigateToDetail = { id ->
                    navController.navigate("journal/$id")
                }
            )
        }

        composable(ProdiDestinations.BUDDHA) {
            BuddhaScreen(
                onNavigateToConversation = { conversationId ->
                    val route = if (conversationId != null) {
                        "${ProdiDestinations.BUDDHA_CONVERSATION}?conversationId=$conversationId"
                    } else {
                        ProdiDestinations.BUDDHA_CONVERSATION
                    }
                    navController.navigate(route)
                },
                onNavigateToHistory = {
                    navController.navigate(ProdiDestinations.BUDDHA_HISTORY)
                }
            )
        }

        composable(ProdiDestinations.STATS) {
            StatsScreen(
                onNavigateToBadges = {
                    navController.navigate(ProdiDestinations.BADGES)
                },
                onNavigateToLeaderboard = {
                    navController.navigate(ProdiDestinations.LEADERBOARD)
                },
                onNavigateToActivityCalendar = {
                    navController.navigate(ProdiDestinations.ACTIVITY_CALENDAR)
                }
            )
        }

        // ==================== Learn Sub-Screens ====================

        composable(
            route = ProdiDestinations.VOCABULARY_LIST,
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "word"
            VocabularyListScreen(
                type = type,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.navigate("vocabulary/$id")
                }
            )
        }

        composable(
            route = ProdiDestinations.VOCABULARY_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            VocabularyDetailScreen(
                vocabularyId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.VOCABULARY_PRACTICE) {
            VocabularyPracticeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.VOCABULARY_SEARCH) {
            VocabularySearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.navigate("vocabulary/$id")
                }
            )
        }

        // ==================== Journal Sub-Screens ====================

        composable(ProdiDestinations.JOURNAL_NEW) {
            JournalNewScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = ProdiDestinations.JOURNAL_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            JournalDetailScreen(
                journalId = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = {
                    navController.navigate("journal_edit/$id")
                },
                onNavigateToBuddha = { journalId ->
                    navController.navigate("${ProdiDestinations.BUDDHA_CONVERSATION}?journalId=$journalId")
                }
            )
        }

        composable(
            route = ProdiDestinations.JOURNAL_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            JournalEditScreen(
                journalId = id,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // ==================== Buddha Sub-Screens ====================

        composable(
            route = "${ProdiDestinations.BUDDHA_CONVERSATION}?conversationId={conversationId}&journalId={journalId}",
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("journalId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId")?.takeIf { it != -1L }
            val journalId = backStackEntry.arguments?.getLong("journalId")?.takeIf { it != -1L }
            BuddhaConversationScreen(
                conversationId = conversationId,
                journalId = journalId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.BUDDHA_HISTORY) {
            BuddhaHistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToConversation = { conversationId ->
                    navController.navigate("${ProdiDestinations.BUDDHA_CONVERSATION}?conversationId=$conversationId")
                }
            )
        }

        // ==================== Future Self Sub-Screens ====================

        composable(ProdiDestinations.FUTURE_SELF) {
            FutureSelfScreen(
                onNavigateToNew = {
                    navController.navigate(ProdiDestinations.FUTURE_SELF_NEW)
                },
                onNavigateToDetail = { id ->
                    navController.navigate("future_self/$id")
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.FUTURE_SELF_NEW) {
            FutureSelfNewScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = ProdiDestinations.FUTURE_SELF_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            FutureSelfDetailScreen(
                letterId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==================== Profile & Settings ====================

        composable(ProdiDestinations.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate(ProdiDestinations.PROFILE_EDIT) },
                onNavigateToSettings = { navController.navigate(ProdiDestinations.SETTINGS) },
                onNavigateToBadges = { navController.navigate(ProdiDestinations.BADGES) }
            )
        }

        composable(ProdiDestinations.PROFILE_EDIT) {
            ProfileEditScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAppearance = { navController.navigate(ProdiDestinations.SETTINGS_APPEARANCE) },
                onNavigateToNotifications = { navController.navigate(ProdiDestinations.SETTINGS_NOTIFICATIONS) },
                onNavigateToAi = { navController.navigate(ProdiDestinations.SETTINGS_AI) },
                onNavigateToData = { navController.navigate(ProdiDestinations.SETTINGS_DATA) },
                onNavigateToAbout = { navController.navigate(ProdiDestinations.SETTINGS_ABOUT) }
            )
        }

        composable(ProdiDestinations.SETTINGS_APPEARANCE) {
            SettingsAppearanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.SETTINGS_NOTIFICATIONS) {
            SettingsNotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.SETTINGS_AI) {
            SettingsAiScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.SETTINGS_DATA) {
            SettingsDataScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.SETTINGS_ABOUT) {
            SettingsAboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ==================== Stats Sub-Screens ====================

        composable(ProdiDestinations.BADGES) {
            BadgesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.LEADERBOARD) {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(ProdiDestinations.ACTIVITY_CALENDAR) {
            ActivityCalendarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
