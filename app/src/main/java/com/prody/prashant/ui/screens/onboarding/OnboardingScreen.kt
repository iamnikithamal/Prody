package com.prody.prashant.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.prody.prashant.ProdiApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val description: String,
    val quote: String,
    val quoteAuthor: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.SelfImprovement,
        title = "Welcome to Prody",
        subtitle = "Transform. Grow. Evolve.",
        description = "A philosophical companion for your journey of self-transformation, blending ancient wisdom with modern technology.",
        quote = "The privilege of a lifetime is to become who you truly are.",
        quoteAuthor = "Carl Jung"
    ),
    OnboardingPage(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = "Expand Your Mind",
        subtitle = "Learn Every Day",
        description = "Discover new vocabulary, timeless proverbs, and wisdom from philosophers like Jiddu Krishnamurti, Alan Watts, Carl Jung, and Buddha.",
        quote = "In the beginner's mind there are many possibilities, but in the expert's there are few.",
        quoteAuthor = "Shunryu Suzuki"
    ),
    OnboardingPage(
        icon = Icons.Default.Psychology,
        title = "Converse with Buddha",
        subtitle = "AI-Powered Wisdom",
        description = "Let Buddha, your philosophical AI mentor drawing from Stoicism, Zen, and Vedanta, guide you toward clarity and insight.",
        quote = "The observer is the observed.",
        quoteAuthor = "Jiddu Krishnamurti"
    ),
    OnboardingPage(
        icon = Icons.Default.Book,
        title = "Journal Your Journey",
        subtitle = "Reflect & Grow",
        description = "Capture your thoughts, track your moods, and receive AI-powered insights to understand yourself better.",
        quote = "Until you make the unconscious conscious, it will direct your life and you will call it fate.",
        quoteAuthor = "Carl Jung"
    ),
    OnboardingPage(
        icon = Icons.Default.ScheduleSend,
        title = "Letters to the Future",
        subtitle = "Time Capsule Messages",
        description = "Write messages to your future self. Plant seeds of commitment and watch yourself grow over time.",
        quote = "Yesterday I was clever, so I wanted to change the world. Today I am wise, so I am changing myself.",
        quoteAuthor = "Rumi"
    ),
    OnboardingPage(
        icon = Icons.Default.EmojiEvents,
        title = "Grow Through Practice",
        subtitle = "Gamified Progress",
        description = "Build streaks, earn badges, level up, and transform consistency into lasting achievement. Small daily actions create extraordinary results.",
        quote = "We are what we repeatedly do. Excellence is not an act, but a habit.",
        quoteAuthor = "Aristotle"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val preferencesManager = remember { ProdiApplication.instance.preferencesManager }

    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isLastPage) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(onboardingPages.lastIndex)
                            }
                        }
                    ) {
                        Text("Skip")
                    }
                }
            }

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnboardingPageContent(
                    page = onboardingPages[page],
                    pageIndex = page
                )
            }

            // Animated Page Indicators
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index

                    // Animated size and color
                    val indicatorSize by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "indicator_size"
                    )

                    val indicatorColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        animationSpec = tween(300),
                        label = "indicator_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(indicatorSize)
                            .clip(RoundedCornerShape(4.dp))
                            .background(indicatorColor)
                    )
                }
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (isLastPage) {
                            scope.launch {
                                preferencesManager.setOnboardingCompleted(true)
                                onOnboardingComplete()
                            }
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.weight(if (pagerState.currentPage > 0) 1f else 2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isLastPage) "Begin Your Journey" else "Next",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "icon_animation")

    // Pulsing glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Subtle breathing animation
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Staggered content appearance
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(pageIndex) {
        showContent = false
        delay(100)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Icon Container with glow
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationY = floatOffset
                },
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(iconScale * 1.1f)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.3f)
                    )
            )

            // Inner container
            Box(
                modifier = Modifier
                    .size((130 * iconScale).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Animated content
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        animationSpec = tween(600),
                        initialOffsetY = { it / 4 }
                    )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle with accent
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = page.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Description
                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Quote card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "\"${page.quote}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— ${page.quoteAuthor}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
