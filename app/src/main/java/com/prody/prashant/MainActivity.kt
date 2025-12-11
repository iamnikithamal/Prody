package com.prody.prashant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.prody.prashant.navigation.ProdiDestinations
import com.prody.prashant.ui.screens.MainScreen
import com.prody.prashant.ui.screens.onboarding.OnboardingScreen
import com.prody.prashant.ui.theme.ProdiTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen
        var keepSplashScreen = true
        installSplashScreen().setKeepOnScreenCondition { keepSplashScreen }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProdiTheme {
                val preferencesManager = remember { ProdiApplication.instance.preferencesManager }
                val hasCompletedOnboarding by preferencesManager.hasCompletedOnboarding
                    .collectAsStateWithLifecycle(initialValue = null)

                // Allow splash to show for a moment, then check onboarding status
                LaunchedEffect(hasCompletedOnboarding) {
                    if (hasCompletedOnboarding != null) {
                        delay(500) // Brief delay for smooth transition
                        keepSplashScreen = false
                    }
                }

                // Show content once we know the onboarding status
                if (hasCompletedOnboarding != null) {
                    ProdiApp(
                        startDestination = if (hasCompletedOnboarding == true) {
                            ProdiDestinations.MAIN
                        } else {
                            ProdiDestinations.ONBOARDING
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProdiApp(
    startDestination: String
) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            composable(ProdiDestinations.ONBOARDING) {
                OnboardingScreen(
                    onOnboardingComplete = {
                        navController.navigate(ProdiDestinations.MAIN) {
                            popUpTo(ProdiDestinations.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(ProdiDestinations.MAIN) {
                MainScreen()
            }
        }
    }
}
