package com.prody.prashant.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.prody.prashant.navigation.ProdiDestinations
import com.prody.prashant.navigation.ProdiNavHost
import com.prody.prashant.navigation.bottomNavItems
import com.prody.prashant.notification.NotificationActionReceiver
import timber.log.Timber

/**
 * Main screen with bottom navigation bar.
 * Contains the primary navigation structure of the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialDeepLinkDestination: String? = null,
    initialEntityId: Long = -1L,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Handle deep link navigation
    LaunchedEffect(initialDeepLinkDestination) {
        if (initialDeepLinkDestination != null) {
            Timber.d("Processing deep link: $initialDeepLinkDestination, entityId: $initialEntityId")

            val route = mapDeepLinkToRoute(initialDeepLinkDestination, initialEntityId)
            if (route != null) {
                try {
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                    Timber.d("Navigated to: $route")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to navigate to $route")
                }
            }
            onDeepLinkConsumed()
        }
    }

    // Determine if we should show the bottom bar
    val showBottomBar = remember(currentDestination) {
        bottomNavItems.any { it.route == currentDestination?.route }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ProdiBottomBar(
                    currentRoute = currentDestination?.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        ProdiNavHost(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}

/**
 * Maps a deep link destination to the appropriate navigation route.
 */
private fun mapDeepLinkToRoute(destination: String, entityId: Long): String? {
    return when (destination) {
        NotificationActionReceiver.DESTINATION_HOME -> ProdiDestinations.HOME
        NotificationActionReceiver.DESTINATION_LEARN -> ProdiDestinations.LEARN
        NotificationActionReceiver.DESTINATION_JOURNAL -> ProdiDestinations.JOURNAL
        NotificationActionReceiver.DESTINATION_BUDDHA -> ProdiDestinations.BUDDHA
        NotificationActionReceiver.DESTINATION_STATS -> ProdiDestinations.STATS
        NotificationActionReceiver.DESTINATION_FUTURE_SELF -> {
            if (entityId > 0) {
                ProdiDestinations.FUTURE_SELF_DETAIL.replace("{id}", entityId.toString())
            } else {
                ProdiDestinations.FUTURE_SELF
            }
        }
        NotificationActionReceiver.DESTINATION_VOCABULARY_PRACTICE -> ProdiDestinations.VOCABULARY_PRACTICE
        NotificationActionReceiver.DESTINATION_JOURNAL_NEW -> ProdiDestinations.JOURNAL_NEW

        // Legacy destination names (used in old notifications)
        "learn" -> ProdiDestinations.LEARN
        "journal" -> ProdiDestinations.JOURNAL
        "buddha" -> ProdiDestinations.BUDDHA
        "future_self" -> ProdiDestinations.FUTURE_SELF
        "stats" -> ProdiDestinations.STATS

        else -> {
            Timber.w("Unknown deep link destination: $destination")
            null
        }
    }
}

@Composable
private fun ProdiBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
