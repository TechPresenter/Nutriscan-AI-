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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyNutritionLog
import com.example.ui.theme.Localization
import com.example.ui.viewmodel.NutriViewModel

@Composable
fun TrackerScreen(
    viewModel: NutriViewModel,
    onNavigateToSettings: () -> Unit
) {
    val languageCode by viewModel.language.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val dailyLogs by viewModel.dailyLogs.collectAsState()
    val weeklyLogs by viewModel.weeklyLogs.collectAsState()

    // Goals states
    val goalCal by viewModel.goalCalories.collectAsState()
    val goalProt by viewModel.goalProtein.collectAsState()
    val goalCarb by viewModel.goalCarbs.collectAsState()
    val goalFat by viewModel.goalFats.collectAsState()

    // Calculate totals of current day
    val totalCal = dailyLogs.sumOf { it.calories }
    val totalProt = dailyLogs.sumOf { it.protein }
    val totalCarb = dailyLogs.sumOf { it.carbs }
    val totalFat = dailyLogs.sumOf { it.fats }

    val calProgress = (if (goalCal > 0) totalCal / goalCal else 0.0).coerceIn(0.0, 1.0)
    val protProgress = (if (goalProt > 0) totalProt / goalProt else 0.0).coerceIn(0.0, 1.0)
    val carbProgress = (if (goalCarb > 0) totalCarb / goalCarb else 0.0).coerceIn(0.0, 1.0)
    val fatProgress = (if (goalFat > 0) totalFat / goalFat else 0.0).coerceIn(0.0, 1.0)

    // Animated progress targets
    val animatedCalProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = calProgress.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "cal_progress"
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tracker_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Date-scroller bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.04f)
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.changeDateOffset(-1) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .testTag("date_prev")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = currentDate,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Diet Logs Tracking Workspace",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.changeDateOffset(1) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .testTag("date_next")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Macros Goals Circular Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("goals_tracker_card"),
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
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = Localization.getString("daily_summary", languageCode),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Big Circular Calorie Tracker
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.size(110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { animatedCalProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 10.dp,
                                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${totalCal.toInt()}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "/${goalCal.toInt()} cal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Macros Row Limits
                            Column(
                                modifier = Modifier.weight(1.2f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MacroBarItem("Protein", totalProt, goalProt, Color(0xFF66BB6A), protProgress, "🍗")
                                MacroBarItem("Carbs", totalCarb, goalCarb, Color(0xFF42A5F5), carbProgress, "🍇")
                                MacroBarItem("Fats", totalFat, goalFat, Color(0xFFFFA726), fatProgress, "🥑")
                            }
                        }
                    }
                }
            }

            // 7-day Calorie History Column chart
            Text(
                text = "Weekly Activity Calories Chart",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                WeeklyVisualChart(weeklyLogs = weeklyLogs, maxGoal = goalCal)
            }

            // Log entry table list header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Logged Items Today (${dailyLogs.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (dailyLogs.isNotEmpty()) {
                    TextButton(onClick = viewModel::setDateToToday) {
                        Text("Jump to Today", fontSize = 12.sp)
                    }
                }
            }

            // Food Log Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dailyLogs.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No logs registered for this date. Scan crops or add details from the Scanner to begin!",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    dailyLogs.forEach { log ->
                        FoodLogItemRow(
                            log = log,
                            onDelete = { viewModel.deleteLog(log) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun MacroBarItem(
    label: String,
    current: Double,
    goal: Float,
    color: Color,
    progress: Double,
    emoji: String
) {
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress.toFloat(),
        animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = "${current.toInt()}/${goal.toInt()}g",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        )
    }
}

@Composable
fun FoodLogItemRow(
    log: DailyNutritionLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_row_item_${log.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.foodName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Consumed Portion: ${log.quantityGrams.toInt()}g",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Text(
                    text = "+${log.calories.toInt()} kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "P: ${log.protein.toInt()}g · C: ${log.carbs.toInt()}g · F: ${log.fats.toInt()}g",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp).testTag("delete_log_${log.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete item log",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Draw a beautiful responsive 7-day bar chart manually!
@Composable
fun WeeklyVisualChart(weeklyLogs: List<DailyNutritionLog>, maxGoal: Float) {
    // Generate dates map for the last 7 days
    val logsByDay = weeklyLogs.groupBy { it.date }
    val daysList = remember {
        val list = mutableListOf<String>()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, -6)
        repeat(7) {
            list.add(sdf.format(cal.time))
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        daysList.forEach { dateString ->
            val logs = logsByDay[dateString] ?: emptyList()
            val totalCal = logs.sumOf { it.calories }
            val fraction = (if (maxGoal > 0) totalCal / maxGoal else 0.0).coerceIn(0.0, 1.2)
            val barHeightFactor = (fraction / 1.2).toFloat() // Scale limit

            val dateLabel = try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("E", java.util.Locale.getDefault())
                val parsed = inputFormat.parse(dateString)
                if (parsed != null) outputFormat.format(parsed) else "Day"
            } catch (e: Exception) {
                "Day"
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text(
                    text = if (totalCal > 0) "${totalCal.toInt()}" else "",
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                val barGradient = if (totalCal > maxGoal) {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                        )
                    )
                } else if (totalCal > 0) {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(barHeightFactor.coerceAtLeast(0.08f))
                        .clip(RoundedCornerShape(8.dp))
                        .background(barGradient)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateLabel.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
