package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val scientificName: String,
    val category: String, // Fruits, Vegetables, Spices, Herbs, Dry Fruits, Seeds, Nuts
    val calories: Double, // kcal per 100g
    val protein: Double,  // g per 100g
    val carbs: Double,    // g per 100g
    val fats: Double,     // g per 100g
    val fiber: Double,    // g per 100g
    
    // Minerals
    val potassium: String = "0 mg",
    val calcium: String = "0 mg",
    val iron: String = "0 mg",
    val magnesium: String = "0 mg",
    val zinc: String = "0 mg",
    val phosphorus: String = "0 mg",
    
    // Vitamins list represented as comma-separated or formatted string (e.g., "Vitamin C: 53mg, Vitamin A: 64mcg")
    val vitamins: String = "N/A",
    
    // Health & Insights
    val tasteProfile: String = "Sweet",
    val origin: String = "Unknown",
    val season: String = "All year",
    val shelfLife: String = "1 week",
    val ayurvedicProperties: String = "Pacifies Vata & Pitta",
    val medicinalUses: String = "Digestive health, anti-inflammatory",
    val healthBenefits: String = "Improves immune function, promotes heart health",
    val precautions: String = "Eat in moderation",
    val allergies: String = "None typical",
    val recommendedDailyIntake: String = "150g",

    // New Requested Attributes
    val glycemicIndex: String = "Low",
    val sideEffects: String = "None typical",
    val bestTimeToConsume: String = "Morning or afternoon",
    val recommendedQuantity: String = "1 medium piece (150g)",
    val storageTips: String = "Keep in cool, dry place or refrigerate",
    val traditionalUses: String = "Traditional general tonic",
    
    // App Tracking properties
    val imageUri: String? = null, // Path to local captured photo/cache
    val scannedAt: Long = System.currentTimeMillis(), // History timestamp
    var isFavorite: Boolean = false
)
