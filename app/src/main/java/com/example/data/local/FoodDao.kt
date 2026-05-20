package com.example.data.local

import androidx.room.*
import com.example.data.model.FoodItem
import com.example.data.model.DailyNutritionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    // --- FoodItem Scans & History ---
    @Query("SELECT * FROM food_items ORDER BY scannedAt DESC")
    fun getAllHistory(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isFavorite = 1 ORDER BY scannedAt DESC")
    fun getFavorites(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodItemById(id: Int): FoodItem?

    @Query("SELECT * FROM food_items WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getFoodItemByName(name: String): FoodItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(item: FoodItem): Long

    @Update
    suspend fun updateFoodItem(item: FoodItem)

    @Delete
    suspend fun deleteFoodItem(item: FoodItem)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFoodItemById(id: Int)

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchFoodItems(query: String): Flow<List<FoodItem>>

    // --- Daily Logs ---
    @Query("SELECT * FROM daily_nutrition_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getLogsByDate(date: String): Flow<List<DailyNutritionLog>>

    @Query("SELECT * FROM daily_nutrition_logs WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getLogsSince(since: Long): Flow<List<DailyNutritionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyLog(log: DailyNutritionLog): Long

    @Delete
    suspend fun deleteDailyLog(log: DailyNutritionLog)

    @Query("DELETE FROM daily_nutrition_logs WHERE id = :id")
    suspend fun deleteDailyLogById(id: Int)
}
