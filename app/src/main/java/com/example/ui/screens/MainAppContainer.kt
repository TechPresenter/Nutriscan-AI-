package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.AdInterstitial
import com.example.ui.viewmodel.NutriViewModel
import com.example.ui.theme.Localization
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

// Nav keys constants
object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val DETAILS = "details"
    const val TRACKER = "tracker"
    const val COMPARE = "compare"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
}

@Composable
fun MainAppContainer(
    viewModel: NutriViewModel,
    navController: NavHostController = rememberNavController()
) {
    val languageCode by viewModel.language.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val activeInterstitial by viewModel.simulatedInterstitialTrigger.collectAsState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Exit app confirm state
    val context = LocalContext.current
    var showExitConfirmDialog by remember { mutableStateOf(false) }

    // Intercept back actions specifically on the main Dashboard view to confirm exit
    BackHandler(enabled = currentRoute == AppRoutes.DASHBOARD) {
        showExitConfirmDialog = true
    }

    // Determine starting destination (Always show splash screen first)
    val startDestination = AppRoutes.SPLASH

    // Identify if the route should show the bottom bar
    val showBottomBar = currentRoute in listOf(
        AppRoutes.DASHBOARD,
        AppRoutes.TRACKER,
        AppRoutes.COMPARE,
        AppRoutes.FAVORITES,
        AppRoutes.SETTINGS
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    AppBottomNavigationBar(
                        currentRoute = currentRoute ?: AppRoutes.DASHBOARD,
                        languageCode = languageCode,
                        onNavigate = { route ->
                            if (route == AppRoutes.SCANNER) {
                                navController.navigate(route)
                            } else {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(
                    bottom = if (showBottomBar) 76.dp else 0.dp
                )
            ) {
                composable(AppRoutes.SPLASH) {
                    SplashScreen(viewModel) { completed ->
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    }
                }

                composable(AppRoutes.ONBOARDING) {
                    OnboardingScreen(viewModel) {
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                }

                composable(AppRoutes.DASHBOARD) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { foodId ->
                            navController.navigate("${AppRoutes.DETAILS}/$foodId")
                        },
                        onNavigateToCompare = {
                            navController.navigate(AppRoutes.COMPARE)
                        }
                    )
                }

                composable(AppRoutes.SCANNER) {
                    ScannerScreen(viewModel) { foodId ->
                        // Navigate direct detail
                        navController.navigate("${AppRoutes.DETAILS}/$foodId") {
                            popUpTo(AppRoutes.SCANNER) { inclusive = true }
                        }
                    }
                }

                composable(
                    route = "${AppRoutes.DETAILS}/{foodId}",
                    arguments = listOf(navArgument("foodId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val foodId = backStackEntry.arguments?.getInt("foodId") ?: -1
                    DetailsScreen(
                        foodId = foodId,
                        viewModel = viewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(AppRoutes.TRACKER) {
                    TrackerScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) }
                    )
                }

                composable(AppRoutes.COMPARE) {
                    ComparisonScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { foodId ->
                            navController.navigate("${AppRoutes.DETAILS}/$foodId")
                        }
                    )
                }

                composable(AppRoutes.FAVORITES) {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onNavigateToDetails = { foodId ->
                            navController.navigate("${AppRoutes.DETAILS}/$foodId")
                        }
                    )
                }

                composable(AppRoutes.SETTINGS) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Global Overlay Interstitial sponsor dialog helper (triggered after scan intervals)
        AdInterstitial(
            visible = activeInterstitial,
            languageCode = languageCode,
            onClose = { viewModel.dismissInterstitial() }
        )

        // Localized Exit Confirmation Dialog
        if (showExitConfirmDialog) {
            val exitTitle = when (languageCode) {
                "es" -> "Quitar Aplicación"
                "fr" -> "Quitter l'application"
                "de" -> "App verlassen"
                "hi" -> "ऐप बंद करें"
                "te" -> "యాప్ నుండి నిష్క్రమించు"
                "mr" -> "अॅप मधून बाहेर पडा"
                "pa" -> "ਐਪ ਤੋਂ ਬਾਹর ਜਾਓ"
                else -> "Exit Application"
            }
            val exitMessage = when (languageCode) {
                "es" -> "¿Está seguro de que desea salir de NutriScan AI?"
                "fr" -> "Voulez-vous vraiment quitter NutriScan AI ?"
                "de" -> "Möchten Sie NutriScan AI wirklich verlassen?"
                "hi" -> "क्या आप निश्चित रूप से NutriScan AI से बाहर निकलना चाहते हैं?"
                "te" -> "మీరు ఖచ్చితంగా NutriScan AI నుండి నిష్క్రమించాలనుకుంటున్నారా?"
                "mr" -> "आपण निश्चितपणे NutriScan AI मधून बाहेर पडू इच्छिता?"
                "pa" -> "ਕੀ ਤੁਸੀਂ ਯਕीਨਨ NutriScan AI ਤੋਂ ਬਾਹর ਜਾਣਾ ਚਾਹੁੰਦੇ ਹੋ?"
                else -> "Are you sure you want to exit NutriScan AI?"
            }
            val exitBtn = when (languageCode) {
                "es" -> "Salir"
                "fr" -> "Quitter"
                "de" -> "Beenden"
                "hi" -> "बाहर निकलें"
                "te" -> "నిష్క్రమించు"
                "mr" -> "बाहेर पडा"
                "pa" -> "ਬਾਹਰ ਜਾਓ"
                else -> "Exit"
            }
            val cancelBtn = when (languageCode) {
                "es" -> "Cancelar"
                "fr" -> "Annuler"
                "de" -> "Abbrechen"
                "hi" -> "रद्द करें"
                "te" -> "రద్దు చేయి"
                "mr" -> "रद्द करा"
                "pa" -> "ਰੱਦ ਕਰੋ"
                else -> "Cancel"
            }

            AlertDialog(
                onDismissRequest = { showExitConfirmDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Exit icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exitTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Text(
                        text = exitMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitConfirmDialog = false
                            (context as? Activity)?.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(exitBtn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showExitConfirmDialog = false }
                    ) {
                        Text(
                            text = cancelBtn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("exit_confirmation_dialog")
            )
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    languageCode: String,
    onNavigate: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("app_bottom_bar")
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val leftItems = listOf(
                Triple(AppRoutes.DASHBOARD, Icons.Default.Home, Localization.getString("home", languageCode)),
                Triple(AppRoutes.TRACKER, Icons.Default.TrackChanges, Localization.getString("tracker", languageCode))
            )
            val rightItems = listOf(
                Triple(AppRoutes.FAVORITES, Icons.Default.Favorite, Localization.getString("favorites", languageCode)),
                Triple(AppRoutes.SETTINGS, Icons.Default.Settings, Localization.getString("settings", languageCode))
            )

            leftItems.forEach { (route, icon, label) ->
                val isSelected = currentRoute == route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(route) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (!isSelected) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Central scan fab action
            Box(
                modifier = Modifier
                    .size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = { onNavigate(AppRoutes.SCANNER) },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("nav_scan_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Trigger AI camera view finder scan",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            rightItems.forEach { (route, icon, label) ->
                val isSelected = currentRoute == route
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(route) }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        if (!isSelected) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
