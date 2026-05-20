package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_nutrition_logs")
data class DailyNutritionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format: YYYY-MM-DD
    val foodName: String,
    val quantityGrams: Double, // Consumed quantity in grams
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fats: Double,
    val timestamp: Long = System.currentTimeMillis()
)
