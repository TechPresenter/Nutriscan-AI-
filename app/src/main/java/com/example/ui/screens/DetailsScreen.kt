package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.ui.components.AdBanner
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    foodId: Int,
    viewModel: NutriViewModel,
    onNavigateBack: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    // Find the item
    val foodItem = scanHistory.find { it.id == foodId }

    // Intake logger sheet state
    var showLogDialog by remember { mutableStateOf(false) }
    var quantityText by remember { mutableStateOf("100") }

    if (foodItem == null) {
        Box(
            modifier = Modifier.fillMaxSize().testTag("details_screen_loading"),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading food profile ID: $foodId...")
        }
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("details_screen_${foodItem.name.lowercase().replace(" ", "_")}"),
        topBar = {
            TopAppBar(
                title = { Text(foodItem.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("details_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(foodItem) },
                        modifier = Modifier.testTag("details_favorite_toggle")
                    ) {
                        Icon(
                            imageVector = if (foodItem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (foodItem.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Main decorative gradient category card header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                getCategoryColor(foodItem.category).copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.White, shape = CircleShape)
                            .border(1.dp, getCategoryColor(foodItem.category), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getCategoryEmoji(foodItem.category),
                            fontSize = 44.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = foodItem.category,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Quick scientific name & details section
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = foodItem.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = foodItem.scientificName,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Fast action: Add intake logger Form with custom premium gradient
                Button(
                    onClick = { showLogDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp), clip = true)
                        .testTag("add_intake_cta")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.getString("add_to_tracker", languageCode),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Nutrient macros dashboard cards row
            val giLower = foodItem.glycemicIndex.lowercase()
            val giColor = when {
                giLower.contains("low") -> Color(0xFF4CAF50)
                giLower.contains("medium") || giLower.contains("med") || giLower.contains("mod") -> Color(0xFFFF9800)
                giLower.contains("high") -> Color(0xFFF44336)
                else -> MaterialTheme.colorScheme.primary
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacroStatMiniCard("Calories", "${foodItem.calories.toInt()} kcal", Color(0xFFEF5350), Modifier.weight(1f))
                    MacroStatMiniCard("Fiber", "${foodItem.fiber} g", Color(0xFF8D6E63), Modifier.weight(1f))
                    MacroStatMiniCard("GI Index", foodItem.glycemicIndex, giColor, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacroStatMiniCard("Protein", "${foodItem.protein} g", Color(0xFF66BB6A), Modifier.weight(1f))
                    MacroStatMiniCard("Carbs", "${foodItem.carbs} g", Color(0xFF42A5F5), Modifier.weight(1f))
                    MacroStatMiniCard("Fats", "${foodItem.fats} g", Color(0xFFFFCA28), Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Banner Ad slots
            AdBanner(isAdFree, languageCode)

            // detailed segmented expandable stats card tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Section a: Crop Profile Properties
                DetailSectionHeader("Core Agronomy & Shelf-life", Icons.Default.Landscape)
                DetailFieldItem(Localization.getString("origin", languageCode), foodItem.origin)
                DetailFieldItem(Localization.getString("season", languageCode), foodItem.season)
                DetailFieldItem(Localization.getString("shelf_life", languageCode), foodItem.shelfLife)
                DetailFieldItem("Storage Tips", foodItem.storageTips)
                DetailFieldItem("Taste Flavor Profile", foodItem.tasteProfile)

                Spacer(modifier = Modifier.height(16.dp))

                // Section b: Detailed lists of minerals (potassium, calcium, iron, magnesium, zinc, and phosphorus)
                DetailSectionHeader("Minerals profile (per 100g)", Icons.Default.Star)
                MineralBadgeField("Potassium", foodItem.potassium, Color(0xFF42A5F5))
                MineralBadgeField("Calcium", foodItem.calcium, Color(0xFF8D6E63))
                MineralBadgeField("Iron", foodItem.iron, Color(0xFF78909C))
                MineralBadgeField("Magnesium", foodItem.magnesium, Color(0xFF26A69A))
                MineralBadgeField("Zinc", foodItem.zinc, Color(0xFFEC407A))
                MineralBadgeField("Phosphorus", foodItem.phosphorus, Color(0xFFAB47BC))

                Spacer(modifier = Modifier.height(16.dp))

                // Section c: Vitamins list
                DetailSectionHeader("Vitamins Breakdown", Icons.Default.FlashOn)
                DetailValueText(foodItem.vitamins)

                Spacer(modifier = Modifier.height(16.dp))

                // Section d: Dosage & timing details
                DetailSectionHeader("Timing & Consumption Guide", Icons.Default.Schedule)
                DetailFieldItem("Best Time to Consume", foodItem.bestTimeToConsume)
                DetailFieldItem("Recommended Quantity", foodItem.recommendedQuantity)
                DetailFieldItem("Daily Intake Allowance", foodItem.recommendedDailyIntake)

                Spacer(modifier = Modifier.height(16.dp))

                // Section e: Ayurvedic & Traditional medicine wellness characteristics
                DetailSectionHeader(Localization.getString("ayurvedic", languageCode), Icons.Default.Spa)
                DetailValueText(foodItem.ayurvedicProperties)

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionHeader("Traditional Uses", Icons.Default.History)
                DetailValueText(foodItem.traditionalUses)

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionHeader(Localization.getString("medicinal", languageCode), Icons.Default.LocalHospital)
                DetailValueText(foodItem.medicinalUses)

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionHeader(Localization.getString("benefits", languageCode), Icons.Default.HealthAndSafety)
                DetailValueText(foodItem.healthBenefits)

                Spacer(modifier = Modifier.height(16.dp))

                // Section f: Safety concerns
                DetailSectionHeader("Safety Precautions & Warnings", Icons.Default.Warning)
                DetailValueText(foodItem.precautions)

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionHeader("Possible Side Effects", Icons.Default.Info)
                DetailValueText(foodItem.sideEffects)

                Spacer(modifier = Modifier.height(16.dp))

                DetailSectionHeader("Allergens Info", Icons.Default.ReportProblem)
                DetailValueText(foodItem.allergies)
            }

            Spacer(modifier = Modifier.height(60.dp))
        }

        // Add to Daily consumption Log tracker sheet overlay dialog
        if (showLogDialog) {
            AlertDialog(
                onDismissRequest = { showLogDialog = false },
                modifier = Modifier.testTag("log_consumption_dialog"),
                confirmButton = {
                    Button(
                        onClick = {
                            val quant = quantityText.toDoubleOrNull() ?: 100.0
                            viewModel.logFoodConsumption(foodItem, quant)
                            showLogDialog = false
                        }
                    ) {
                        Text(Localization.getString("save", languageCode))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogDialog = false }) {
                        Text(Localization.getString("cancel", languageCode))
                    }
                },
                title = { Text(Localization.getString("add_to_tracker", languageCode), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = Localization.getString("amount_grams", languageCode),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quantity_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            suffix = { Text("g") }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun MacroStatMiniCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(76.dp)
            .border(1.dp, color.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DetailSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun DetailFieldItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DetailValueText(value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Text(
            text = value,
            fontSize = 12.sp,
            modifier = Modifier.padding(12.dp),
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MineralBadgeField(name: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(name, fontSize = 12.sp, modifier = Modifier.width(100.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
