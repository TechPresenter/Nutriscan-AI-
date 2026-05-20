package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyNutritionLog
import com.example.data.model.FoodItem
import com.example.ui.components.AdBanner
import com.example.ui.components.AdNative
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel
import com.example.ui.viewmodel.ScanUiState
import com.example.ui.viewmodel.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NutriViewModel,
    onNavigateToDetails: (foodId: Int) -> Unit,
    onNavigateToCompare: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val themeSelected by viewModel.theme.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val dailyLogs by viewModel.dailyLogs.collectAsState()

    // Goals states
    val goalCal by viewModel.goalCalories.collectAsState()
    val goalProt by viewModel.goalProtein.collectAsState()
    val goalCarb by viewModel.goalCarbs.collectAsState()
    val goalFat by viewModel.goalFats.collectAsState()

    // Voice simulation
    var showVoiceDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showChatDialog by remember { mutableStateOf(false) }

    val lookupUiState by viewModel.lookupUiState.collectAsState()

    // Handle lookup automatic navigation
    LaunchedEffect(lookupUiState) {
        if (lookupUiState is ScanUiState.Success) {
            val food = (lookupUiState as ScanUiState.Success).foodItem
            if (food.name != "Unknown Details" && food.name != "Unknown") {
                onNavigateToDetails(food.id)
                viewModel.clearLookupUiState()
            }
        }
    }

    // Filtering categories items
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Calculate logged totals
    val loggedCal = dailyLogs.sumOf { it.calories }
    val loggedProt = dailyLogs.sumOf { it.protein }
    val loggedCarb = dailyLogs.sumOf { it.carbs }
    val loggedFat = dailyLogs.sumOf { it.fats }

    val calProgress = (if (goalCal > 0) loggedCal / goalCal else 0.0).coerceIn(0.0, 1.0)

    val categories = listOf(
        CategoryItem("Fruits", "🍎", Color(0xFFFFCDD2)),
        CategoryItem("Vegetables", "🥦", Color(0xFFC8E6C9)),
        CategoryItem("Spices & Herbs", "🌶️", Color(0xFFFFE0B2)),
        CategoryItem("Dry Fruits", "🥜", Color(0xFFD7CCC8)),
        CategoryItem("Seeds", "🌻", Color(0xFFFFF9C4)),
        CategoryItem("Nuts", "🌰", Color(0xFFF1F8E9))
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(bottom = 10.dp)
                ) {
                    // Header Brand Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Logo Box
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = "NutriScan Logo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = Localization.getString("app_title", languageCode),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "Aesthetic Wellness AI",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Right header switchers
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Language Quick Cycle Badge
                            val currentLang = languageCode
                            val currentThemeSelected = themeSelected
                            val nextLang = when (currentLang) {
                                "en" -> "es"
                                "es" -> "fr"
                                "fr" -> "hi"
                                else -> "en"
                            }
                            val nextTheme = when (currentThemeSelected) {
                                "dark" -> "light"
                                else -> "dark"
                            }

                            FilledTonalIconButton(
                                onClick = { showLangDialog = true },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Language switcher",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 4.dp, y = 4.dp)
                                            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 2.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            currentLang.uppercase(),
                                            fontSize = 7.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onTertiary
                                        )
                                    }
                                }
                            }

                            FilledTonalIconButton(
                                onClick = { viewModel.setTheme(nextTheme) },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (currentThemeSelected == "dark") Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Theme toggle",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Compare action quick access
                            FilledTonalIconButton(
                                onClick = onNavigateToCompare,
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = "Compare foods",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Integrated Sticky Search Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { 
                                viewModel.setSearchQuery(it)
                                if (it.isNotBlank()) {
                                    selectedCategory = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_bar"),
                            placeholder = {
                                Text(
                                    text = Localization.getString("search_placeholder", languageCode),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { showVoiceDialog = true },
                                        modifier = Modifier.testTag("voice_input_trigger")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Voice search mic",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                        // Load sample data trigger if list is pristine
                        if (scanHistory.isEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalIconButton(
                                onClick = { viewModel.loadSampleData() },
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .size(40.dp)
                                    .testTag("load_sample_data")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Load Sample Data",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Google Banner Ad slots
            AdBanner(isAdFree, languageCode)

            // 0. AI Chatbot Advisor Entrance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { showChatDialog = true }
                    .testTag("chat_advisor_entrance_card"),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Botanical Chat",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1.0f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Botanical AI Chatbot",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE VOICE 🎙️",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ask anything about foods, fruits, vegetables, traditional medicines, herbs, seeds & spices!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Chat",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 1. Calorie summary Progress Gauge Section (Premium organic gradient background card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("calorie_progress_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gauge circle
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { calProgress.toFloat() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("circular_calorie_indicator"),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${loggedCal.toInt()}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "kcal",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Targets text rows
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = Localization.getString("daily_summary", languageCode),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Goal Target Limit: ${goalCal.toInt()} kcal",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Linear macros bars
                            MacroCompactRow("Prot", loggedProt, goalProt, Color(0xFF66BB6A))
                            MacroCompactRow("Carb", loggedCarb, goalCarb, Color(0xFF42A5F5))
                            MacroCompactRow("Fats", loggedFat, goalFat, Color(0xFFFFA726))
                        }
                    }
                }
            }

            // 2. Browse categories List Section
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = Localization.getString("categories", languageCode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat.name
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else cat.bgColor.copy(alpha = 0.25f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else cat.bgColor.copy(alpha = 0.6f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedCategory = if (isSelected) null else cat.name
                                }
                                .testTag("category_${cat.name.lowercase()}"),
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            // 3. Main Scanned List Section
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = if (searchQuery.isNotBlank()) "Search Results" else Localization.getString("history", languageCode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (searchQuery.isNotBlank()) {
                    // Quick Gemini Lookup Card for active search queries!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                viewModel.lookupFoodDetailsByName(searchQuery) { newId ->
                                    onNavigateToDetails(newId)
                                }
                            }
                            .testTag("ai_details_lookup_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Details Lookup",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Details Scanner: \"$searchQuery\"",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Tap to fetch botanical & nutrition files from AI Database",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                val itemsToShow = if (searchQuery.isNotBlank()) {
                    searchResults
                } else {
                    scanHistory
                }.filter { selectedCategory == null || it.category == selectedCategory }

                if (itemsToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Localization.getString("no_scans", languageCode),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    itemsToShow.forEach { food ->
                        FoodHistoryRowItem(
                            food = food,
                            onItemClick = { onNavigateToDetails(food.id) },
                            onToggleFav = { viewModel.toggleFavorite(food) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp)) // Space above bottom navbar padding
        }

        // Voice microphone Simulated Listening overlay Dialog
        SimulatedVoiceDialog(
            visible = showVoiceDialog,
            languageCode = languageCode,
            onClose = { showVoiceDialog = false },
            onTextDetected = { query ->
                viewModel.setSearchQuery(query)
                showVoiceDialog = false
            }
        )

        // Botanical AI Chatbot Dialog
        AIBotanicalChatDialog(
            visible = showChatDialog,
            viewModel = viewModel,
            languageCode = languageCode,
            onClose = { showChatDialog = false }
        )

        if (showLangDialog) {
            LanguageSelectorDialog(
                currentLang = languageCode,
                onDismiss = { showLangDialog = false },
                onSelectLang = { selected ->
                    viewModel.setLanguage(selected)
                    showLangDialog = false
                }
            )
        }

        if (lookupUiState is ScanUiState.Scanning) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                modifier = Modifier.testTag("lookup_loading_dialog"),
                title = {
                    Text(
                        text = "AI Botanical Lookup",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Analyzing \"$searchQuery\"...",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Compiling macronutrients, vitamins, ayurvedic energetics, and therapeutic botanical profiles...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            )
        }

        if (lookupUiState is ScanUiState.Error) {
            AlertDialog(
                onDismissRequest = { viewModel.clearLookupUiState() },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearLookupUiState() }) {
                        Text("OK")
                    }
                },
                modifier = Modifier.testTag("lookup_error_dialog"),
                title = {
                    Text(
                        text = "Lookup Error",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Text(
                        text = (lookupUiState as ScanUiState.Error).message,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}

@Composable
fun FoodHistoryRowItem(
    food: FoodItem,
    onItemClick: () -> Unit,
    onToggleFav: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onItemClick() }
            .testTag("food_row_${food.name.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Premium custom botanical style circular emoji badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(food.category).copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .border(1.dp, getCategoryColor(food.category).copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCategoryEmoji(food.category),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = food.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = food.scientificName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Premium calorie badge pill
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${food.calories.toInt()} kcal",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = food.category,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }

            // High contrast customized like state
            IconButton(
                onClick = onToggleFav,
                modifier = Modifier.testTag("fav_btn_${food.id}")
            ) {
                Icon(
                    imageVector = if (food.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (food.isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun MacroCompactRow(
    label: String,
    current: Double,
    goal: Float,
    color: Color
) {
    val progress = (if (goal > 0) current / goal else 0.0).coerceIn(0.0, 1.0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(34.dp)
        )
        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier = Modifier
                .weight(1.0f)
                .height(4.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            "${current.toInt()}/${goal.toInt()}g",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimulatedVoiceDialog(
    visible: Boolean,
    languageCode: String,
    onClose: () -> Unit,
    onTextDetected: (String) -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    var voiceText by remember { mutableStateOf("Listening...") }
    var isRecordingRealMic by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Quick-tap crop simulation targets divided by Category
    val fruits = listOf("Red Apple", "Sweet Mango", "Ripe Banana")
    val vegetables = listOf("Fresh Broccoli", "Tomato", "Spinach")
    val spices = listOf("Cardamom", "Ginger", "Fresh Garlic", "Cinnamon", "Cloves", "Turmeric", "Cumin Seeds")

    // Speech permissions state
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasRecordPermission = granted
            if (granted) {
                voiceText = "Microphone granted! Click mic icon to start speaking."
            }
        }
    )

    fun startListeningWithSpeechEngine() {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceText = "Voice recognition not supported on this device. Please tap suggestion shortcut!"
            return
        }

        try {
            if (speechRecognizer != null) {
                speechRecognizer?.destroy()
            }
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (languageCode == "hi") "hi-IN" else if (languageCode == "es") "es-ES" else Locale.getDefault().language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isRecordingRealMic = true
                    voiceText = "Speak now..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isRecordingRealMic = false
                }
                override fun onError(error: Int) {
                    isRecordingRealMic = false
                    voiceText = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Please tap suggestion below!"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Timeout. Use direct suggestions below!"
                        else -> "Real-time speech ready. Or tap any key crop below index!"
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val resultText = matches[0]
                        voiceText = resultText
                        onTextDetected(resultText)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        voiceText = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            recognizer.startListening(intent)
            speechRecognizer = recognizer
        } catch (e: Exception) {
            voiceText = "Hardware speech issue. Please tap suggestion shortcut!"
        }
    }

    // Auto-trigger micro recognition if permission is ready
    LaunchedEffect(key1 = hasRecordPermission) {
        if (hasRecordPermission) {
            startListeningWithSpeechEngine()
        } else {
            voiceText = "Microphone disabled. Grant access or tap shortcut below."
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.testTag("voice_dialog"),
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(Localization.getString("cancel", languageCode), color = MaterialTheme.colorScheme.primary)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Localization.getString("voice_input", languageCode),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Aesthetic Botanical Voice Search",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Recording pulses
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .size(84.dp)
                        .scale(if (isRecordingRealMic) scale else 1.0f)
                        .background(
                            color = if (isRecordingRealMic) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (isRecordingRealMic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable { startListeningWithSpeechEngine() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Recording mic",
                        tint = if (isRecordingRealMic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = voiceText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecordingRealMic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion titles
                Text(
                    text = "Suggested Voice Shortcuts (For Testing):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // segment Fruits
                Text(
                    text = "🍎 Fruits",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    fruits.forEach { name ->
                        SuggestionPill(name) {
                            voiceText = name
                            onTextDetected(name)
                        }
                    }
                }

                // segment Vegetables
                Text(
                    text = "🥦 Vegetables",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    vegetables.forEach { name ->
                        SuggestionPill(name) {
                            voiceText = name
                            onTextDetected(name)
                        }
                    }
                }

                // segment Spices
                Text(
                    text = "🌶️ Spices & Herbs",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    spices.forEach { name ->
                        SuggestionPill(name) {
                            voiceText = name
                            onTextDetected(name)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SuggestionPill(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(vertical = 3.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Hearing,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class CategoryItem(val name: String, val emoji: String, val bgColor: Color)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Fruits" -> Color(0xFFFFCDD2)
        "Vegetables" -> Color(0xFFC8E6C9)
        "Spices & Herbs" -> Color(0xFFFFE0B2)
        "Dry Fruits" -> Color(0xFFD7CCC8)
        "Seeds" -> Color(0xFFFFF9C4)
        "Nuts" -> Color(0xFFF1F8E9)
        else -> Color(0xFFE0E0E0)
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Fruits" -> "🍎"
        "Vegetables" -> "🥦"
        "Spices & Herbs" -> "🌶️"
        "Dry Fruits" -> "🥜"
        "Seeds" -> "🌻"
        "Nuts" -> "🌰"
        else -> "🥗"
    }
}

@Composable
fun LanguageSelectorDialog(
    currentLang: String,
    onDismiss: () -> Unit,
    onSelectLang: (String) -> Unit
) {
    val languages = listOf(
        Triple("en", "English", "🇺🇸"),
        Triple("hi", "हिंदी (Hindi)", "🇮🇳"),
        Triple("mr", "मराठी (Marathi)", "🇮🇳"),
        Triple("pa", "ਪੰਜਾਬੀ (Punjabi)", "🇮🇳"),
        Triple("te", "తెలుగు (Telugu)", "🇮🇳"),
        Triple("es", "Español (Spanish)", "🇪🇸"),
        Triple("fr", "Français (French)", "🇫🇷"),
        Triple("de", "Deutsch (German)", "🇩🇪")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = Localization.getString("cancel", currentLang), fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text(
                text = Localization.getString("language_switch", currentLang),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { (code, name, flag) ->
                        val isSelected = currentLang == code
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLang(code) }
                                .testTag("lang_dialog_option_$code"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = flag, fontSize = 24.sp)
                                Text(
                                    text = name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                              )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIBotanicalChatDialog(
    visible: Boolean,
    viewModel: NutriViewModel,
    languageCode: String,
    onClose: () -> Unit
) {
    if (!visible) return

    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isRecordingRealMic by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var voiceStatusText by remember { mutableStateOf("") }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micScale"
    )

    // Suggestion questions
    val suggestions = listOf(
        "Is ginger medicinal?",
        "Calories in almonds?",
        "Allicin benefits of garlic?",
        "Ayurvedic use of turmeric",
        "Side effects of nutmeg",
        "Are sunflower seeds edible?"
    )

    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasRecordPermission = granted
            if (granted) {
                voiceStatusText = "Microphone ready! Touch mic to speak."
            }
        }
    )

    fun startListeningWithSpeechEngine() {
        if (!hasRecordPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceStatusText = "Speech engine not available here."
            return
        }

        try {
            if (speechRecognizer != null) {
                speechRecognizer?.destroy()
            }
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (languageCode == "hi") "hi-IN" else if (languageCode == "es") "es-ES" else Locale.getDefault().language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isRecordingRealMic = true
                    voiceStatusText = "Speaking..."
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isRecordingRealMic = false
                }
                override fun onError(error: Int) {
                    isRecordingRealMic = false
                    voiceStatusText = "Error recording. Try tap shortcut!"
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val resultText = matches[0]
                        textInput = resultText
                        viewModel.sendChatMessage(resultText)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        textInput = matches[0]
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            recognizer.startListening(intent)
            speechRecognizer = recognizer
        } catch (e: Exception) {
            voiceStatusText = "Speech Error: ${e.localizedMessage}"
        }
    }

    DisposableEffect(Unit) {
        // Pre-fill intro message if vacant
        if (viewModel.chatMessages.value.isEmpty()) {
            viewModel.sendChatMessage("Hello! Help me identify standard crops and traditional herbs.", "Greeting helper system instruction")
        }
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("botanical_chat_dialog"),
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onClose) {
                Text(Localization.getString("close_ad", languageCode), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Chat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Botanical AI Chatbot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "🌿 Talk Live with our AI Ayurvedic Expert & Botanist 🟢",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .height(420.dp)
            ) {
                // Chats timeline List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    val scrollState = rememberScrollState()
                    LaunchedEffect(chatMessages.size, chatLoading) {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (chatMessages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Ask anything about foods, herbs, spices, seeds or nuts. E.g., 'What are turmeric's medicinal uses?' or 'Glycemic index of watermelon?'",
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            chatMessages.forEach { msg ->
                                val bubbleBg = if (msg.isUser) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surface,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                }
                                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                                val bubbleBorder = if (msg.isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                Column(modifier = Modifier.align(alignment).fillMaxWidth(0.9f)) {
                                    Surface(
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (msg.isUser) 16.dp else 2.dp,
                                            bottomEnd = if (msg.isUser) 2.dp else 16.dp
                                        ),
                                        border = bubbleBorder,
                                        shadowElevation = 2.dp,
                                        modifier = Modifier.align(alignment)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(bubbleBg)
                                                .padding(12.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (msg.isUser) Icons.Default.AccountCircle else Icons.Default.AutoAwesome,
                                                        contentDescription = null,
                                                        tint = if (msg.isUser) Color.White else MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (msg.isUser) "You" else "Advisor AI",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (msg.isUser) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = msg.text,
                                                    fontSize = 12.sp,
                                                    color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (chatLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.Start)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Botanical AI is compiling files...", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion pills row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(suggestions) { text ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            modifier = Modifier.clickable {
                                textInput = text
                                viewModel.sendChatMessage(text)
                            }
                        ) {
                            Text(
                                text = text,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // TextInput, Clear button, and Mic triggers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speech voice trigger
                    FilledTonalIconButton(
                        onClick = { startListeningWithSpeechEngine() },
                        modifier = Modifier
                            .size(46.dp)
                            .scale(if (isRecordingRealMic) scale else 1.0f),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isRecordingRealMic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = if (isRecordingRealMic) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Ask a question...", fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        trailingIcon = {
                            if (textInput.isNotEmpty()) {
                                IconButton(onClick = { textInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendChatMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (voiceStatusText.isNotEmpty()) {
                    Text(
                        text = voiceStatusText,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
        }
    )
}
