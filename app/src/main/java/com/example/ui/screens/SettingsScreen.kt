package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NutriViewModel
) {
    val languageCode by viewModel.language.collectAsState()
    val themeSelected by viewModel.theme.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()

    // Goals states
    val calVal by viewModel.goalCalories.collectAsState()
    val protVal by viewModel.goalProtein.collectAsState()
    val carbVal by viewModel.goalCarbs.collectAsState()
    val fatVal by viewModel.goalFats.collectAsState()

    // Local inputs for modifying goals limits
    var inputCal by remember { mutableStateOf("") }
    var inputProt by remember { mutableStateOf("") }
    var inputCarb by remember { mutableStateOf("") }
    var inputFat by remember { mutableStateOf("") }

    // On setup load current values once
    LaunchedEffect(calVal) {
        inputCal = calVal.toInt().toString()
        inputProt = protVal.toInt().toString()
        inputCarb = carbVal.toInt().toString()
        inputFat = fatVal.toInt().toString()
    }

    var showGoalsSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = { Text(Localization.getString("settings", languageCode), fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Theme selection card row button group
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        Localization.getString("app_theme", languageCode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionRowButton(
                            label = "Light",
                            isSelected = themeSelected == "light",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTheme("light")
                        }

                        ThemeOptionRowButton(
                            label = "Dark",
                            isSelected = themeSelected == "dark",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTheme("dark")
                        }

                        ThemeOptionRowButton(
                            label = "System",
                            isSelected = themeSelected == "system",
                            modifier = Modifier.weight(1f)
                        ) {
                            viewModel.setTheme("system")
                        }
                    }
                }
            }

            // 2. Language selection card row buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        Localization.getString("language_switch", languageCode),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOptionButton("English", languageCode == "en", Modifier.weight(1f)) { viewModel.setLanguage("en") }
                        LanguageOptionButton("Español", languageCode == "es", Modifier.weight(1f)) { viewModel.setLanguage("es") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOptionButton("Français", languageCode == "fr", Modifier.weight(1f)) { viewModel.setLanguage("fr") }
                        LanguageOptionButton("Deutsch", languageCode == "de", Modifier.weight(1f)) { viewModel.setLanguage("de") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOptionButton("हिंदी", languageCode == "hi", Modifier.weight(1f)) { viewModel.setLanguage("hi") }
                        LanguageOptionButton("मराठी", languageCode == "mr", Modifier.weight(1f)) { viewModel.setLanguage("mr") }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LanguageOptionButton("ਪੰਜਾਬੀ", languageCode == "pa", Modifier.weight(1f)) { viewModel.setLanguage("pa") }
                        LanguageOptionButton("తెలుగు", languageCode == "te", Modifier.weight(1f)) { viewModel.setLanguage("te") }
                    }
                }
            }

            // 3. Nutrition custom goals card settings options
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Nutritional Macro Goals",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Calories: ${calVal.toInt()} kcal · Prot: ${protVal.toInt()}g",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showGoalsSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("edit_goals_trigger")
                    ) {
                        Text("Update Target", fontSize = 12.sp)
                    }
                }
            }

            // 4. In App Purchases simulator (Ad Free toggle premium licensing)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdFree) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isAdFree) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (isAdFree) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "NutriScan AI Premium Pro",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isAdFree) "Lifetime Premium License Active" else "Support our servers ad-free",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isAdFree,
                            onCheckedChange = { viewModel.setAdFree(it) },
                            modifier = Modifier.testTag("ad_free_toggle_switch")
                        )
                    }
                }
            }

            // 5. App Store actions (Rate App, Share with friends, Privacy Policy)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsActionRowItem("Rate App Store", Icons.Default.Star, "settings_rate") {}
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRowItem("Share NutriScan with community", Icons.Default.Share, "settings_share") {}
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingsActionRowItem("Privacy Policy & Terms", Icons.Default.Security, "settings_privacy") {}
                }
            }

            // Developer Credentials Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Architect & Design Lead",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prashant Singh Kushwaha",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), modifier = Modifier.width(100.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power icon",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Powered by Appsgain Technologies",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 2.0.0 · Active Analytics Feed",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Goals editor sheet dialog
        if (showGoalsSheet) {
            AlertDialog(
                onDismissRequest = { showGoalsSheet = false },
                modifier = Modifier.testTag("edit_goals_dialog"),
                confirmButton = {
                    Button(
                        onClick = {
                            val cal = inputCal.toFloatOrNull() ?: 2000f
                            val prot = inputProt.toFloatOrNull() ?: 130f
                            val carb = inputCarb.toFloatOrNull() ?: 250f
                            val fat = inputFat.toFloatOrNull() ?: 65f
                            viewModel.setGoals(cal, prot, carb, fat)
                            showGoalsSheet = false
                        }
                    ) {
                        Text(Localization.getString("save", languageCode))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoalsSheet = false }) {
                        Text(Localization.getString("cancel", languageCode))
                    }
                },
                title = { Text("Update Macro Boundaries Target", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GoalInputField("Calories Limit (kcal)", inputCal) { inputCal = it }
                        GoalInputField("Protein Limit (grams)", inputProt) { inputProt = it }
                        GoalInputField("Carbs Limit (grams)", inputCarb) { inputCarb = it }
                        GoalInputField("Fats Limit (grams)", inputFat) { inputFat = it }
                    }
                }
            )
        }
    }
}

@Composable
fun ThemeOptionRowButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp).testTag("theme_btn_${label.lowercase()}"),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LanguageOptionButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp).testTag("lang_btn_${label.lowercase()}"),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SettingsActionRowItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun GoalInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(3.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}
