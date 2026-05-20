package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: NutriViewModel,
    onSplashFinished: (completedOnboarding: Boolean) -> Unit
) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val languageCode by viewModel.language.collectAsState()
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1500)
        onSplashFinished(onboardingCompleted)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B5E20), // Jungle Organic Green
                        Color(0xFF0D5330)  // Deep Forest Green
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(800)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color.White.copy(alpha = 0.15f), shape = CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "App Logo",
                        tint = Color(0xFFAEEA00), // Vibrant leafy Lime
                        modifier = Modifier.size(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = Localization.getString("app_title", languageCode),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "AI-Powered Nutrition Science",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: NutriViewModel,
    onOnboardingFinished: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val slides = listOf(
        OnboardingSlideData(
            titleKey = "onboarding_1_title",
            descKey = "onboarding_1_desc",
            icon = Icons.Default.Spa,
            accentColor = Color(0xFFC5E1A5)
        ),
        OnboardingSlideData(
            titleKey = "onboarding_2_title",
            descKey = "onboarding_2_desc",
            icon = Icons.Default.LocalFlorist,
            accentColor = Color(0xFF81C784)
        ),
        OnboardingSlideData(
            titleKey = "onboarding_3_title",
            descKey = "onboarding_3_desc",
            icon = Icons.Default.Favorite,
            accentColor = Color(0xFFFF8A80)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("onboarding_screen")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val slide = slides[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    slide.accentColor.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        tint = slide.accentColor,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = Localization.getString(slide.titleKey, languageCode),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Localization.getString(slide.descKey, languageCode),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // Skip Button (Top Right)
        if (pagerState.currentPage < 2) {
            Text(
                text = Localization.getString("skip", languageCode),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .clickable {
                        viewModel.completeOnboarding()
                        onOnboardingFinished()
                    }
                    .testTag("skip_onboarding"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Bottom section with dot indicators and Next/GetStarted Button
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 18.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }

            // Next button
            if (pagerState.currentPage < 2) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                        .testTag("next_slide")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = Localization.getString("next", languageCode),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                Button(
                    onClick = {
                        viewModel.completeOnboarding()
                        onOnboardingFinished()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("finish_onboarding")
                ) {
                    Text(
                        text = Localization.getString("get_started", languageCode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

data class OnboardingSlideData(
    val titleKey: String,
    val descKey: String,
    val icon: ImageVector,
    val accentColor: Color
)
