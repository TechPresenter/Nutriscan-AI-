package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel

@Composable
fun ComparisonScreen(
    viewModel: NutriViewModel,
    onNavigateToDetails: (foodId: Int) -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    var food1 by remember { mutableStateOf<FoodItem?>(null) }
    var food2 by remember { mutableStateOf<FoodItem?>(null) }

    // Dropdown visibility states
    var showDropdown1 by remember { mutableStateOf(false) }
    var showDropdown2 by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("comparison_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = Localization.getString("compare", languageCode),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Side-by-Side Analytical Nutrition Panel",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Selector cards Row with centered floating 'VS' decorative badge
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item 1 box selector
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clickable { showDropdown1 = true }
                            .testTag("compare_select_btn_1"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (food1 == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Select Food A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(getCategoryEmoji(food1!!.category), fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(food1!!.name, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                    Text("${food1!!.calories.toInt()} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            // Food 1 Dropdown
                            DropdownMenu(
                                expanded = showDropdown1,
                                onDismissRequest = { showDropdown1 = false },
                                modifier = Modifier.testTag("compare_dropdown_1")
                            ) {
                                if (scanHistory.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No scan history. Add some first!") },
                                        onClick = { showDropdown1 = false }
                                    )
                                }
                                scanHistory.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text("${getCategoryEmoji(item.category)} ${item.name}") },
                                        onClick = {
                                            food1 = item
                                            showDropdown1 = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Item 2 box selector
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clickable { showDropdown2 = true }
                            .testTag("compare_select_btn_2"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (food2 == null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Select Food B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(getCategoryEmoji(food2!!.category), fontSize = 32.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(food2!!.name, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1)
                                    Text("${food2!!.calories.toInt()} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            // Food 2 Dropdown
                            DropdownMenu(
                                expanded = showDropdown2,
                                onDismissRequest = { showDropdown2 = false },
                                modifier = Modifier.testTag("compare_dropdown_2")
                            ) {
                                if (scanHistory.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No scan history. Add some first!") },
                                        onClick = { showDropdown2 = false }
                                    )
                                }
                                scanHistory.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text("${getCategoryEmoji(item.category)} ${item.name}") },
                                        onClick = {
                                            food2 = item
                                            showDropdown2 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Center Float overlay VS Badge
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.background),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "VS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Compare details block tabular representation
            if (food1 == null || food2 == null) {
                // Prompt picker
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Scale, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Please select both products to discover side-by-side comparative health stats and minerals profiles.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Side by Side comparison rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompareStatTableSection("ENERGY & MACROS", listOf(
                        CompareRowData("Calories", "${food1!!.calories.toInt()} kcal", "${food2!!.calories.toInt()} kcal", food1!!.calories > food2!!.calories),
                        CompareRowData("Protein", "${food1!!.protein} g", "${food2!!.protein} g", food1!!.protein > food2!!.protein),
                        CompareRowData("Carbs", "${food1!!.carbs} g", "${food2!!.carbs} g", food1!!.carbs > food2!!.carbs),
                        CompareRowData("Dietary Fiber", "${food1!!.fiber} g", "${food2!!.fiber} g", food1!!.fiber > food2!!.fiber),
                        CompareRowData("Fats", "${food1!!.fats} g", "${food2!!.fats} g", food1!!.fats < food2!!.fats) // Less fat preferred
                    ))

                    CompareStatTableSection("MINERALS CONCENTRATIONS", listOf(
                        CompareRowData("Potassium", food1!!.potassium, food2!!.potassium, false),
                        CompareRowData("Calcium", food1!!.calcium, food2!!.calcium, false),
                        CompareRowData("Iron", food1!!.iron, food2!!.iron, false),
                        CompareRowData("Magnesium", food1!!.magnesium, food2!!.magnesium, false),
                        CompareRowData("Zinc", food1!!.zinc, food2!!.zinc, false),
                        CompareRowData("Phosphorus", food1!!.phosphorus, food2!!.phosphorus, false)
                    ))

                    CompareStatTableSection("AGRONOMY PROFILE", listOf(
                        CompareRowData("Scientific Latin Name", food1!!.scientificName, food2!!.scientificName, false),
                        CompareRowData("Seasonal Availability", food1!!.season, food2!!.season, false),
                        CompareRowData("Shelf Life", food1!!.shelfLife, food2!!.shelfLife, false)
                    ))

                    // Quick Clear Button
                    OutlinedButton(
                        onClick = {
                            food1 = null
                            food2 = null
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset Compare Deck")
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun CompareStatTableSection(
    title: String,
    rows: List<CompareRowData>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            rows.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left parameter label
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(row.paramName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // A value
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = row.valA,
                            fontSize = 12.sp,
                            fontWeight = if (row.preferredA) FontWeight.Black else FontWeight.Medium,
                            color = if (row.preferredA) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Divider mark
                    Text(":", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    // B value
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = row.valB,
                            fontSize = 12.sp,
                            fontWeight = if (row.preferredB) FontWeight.Black else FontWeight.Medium,
                            color = if (row.preferredB) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (idx < rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

data class CompareRowData(
    val paramName: String,
    val valA: String,
    val valB: String,
    val preferredA: Boolean = false,
    val preferredB: Boolean = !preferredA && valA != valB
)
