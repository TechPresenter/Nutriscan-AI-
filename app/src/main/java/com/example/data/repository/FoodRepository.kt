package com.example.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.FoodDao
import com.example.data.model.DailyNutritionLog
import com.example.data.model.FoodItem
import com.example.data.remote.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodRepository(private val foodDao: FoodDao) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Flow streams for UI
    val scanHistory: Flow<List<FoodItem>> = foodDao.getAllHistory()
    val favorites: Flow<List<FoodItem>> = foodDao.getFavorites()

    // Search query stream
    fun searchHistory(query: String): Flow<List<FoodItem>> = foodDao.searchFoodItems(query)

    // Log tracking streams
    fun getDailyLogs(date: String): Flow<List<DailyNutritionLog>> = foodDao.getLogsByDate(date)
    fun getLogsSince(since: Long): Flow<List<DailyNutritionLog>> = foodDao.getLogsSince(since)

    // DB CRUD helper functions
    suspend fun insertFoodItem(item: FoodItem): Long = withContext(Dispatchers.IO) {
        foodDao.insertFoodItem(item)
    }

    suspend fun updateFoodItem(item: FoodItem) = withContext(Dispatchers.IO) {
        foodDao.updateFoodItem(item)
    }

    suspend fun deleteFoodItem(item: FoodItem) = withContext(Dispatchers.IO) {
        foodDao.deleteFoodItem(item)
    }

    suspend fun deleteFoodItemById(id: Int) = withContext(Dispatchers.IO) {
        foodDao.deleteFoodItemById(id)
    }

    suspend fun getFoodItemById(id: Int): FoodItem? = withContext(Dispatchers.IO) {
        foodDao.getFoodItemById(id)
    }

    suspend fun getFoodItemByName(name: String): FoodItem? = withContext(Dispatchers.IO) {
        foodDao.getFoodItemByName(name)
    }

    suspend fun insertDailyLog(log: DailyNutritionLog): Long = withContext(Dispatchers.IO) {
        foodDao.insertDailyLog(log)
    }

    suspend fun deleteDailyLog(log: DailyNutritionLog) = withContext(Dispatchers.IO) {
        foodDao.deleteDailyLog(log)
    }

    suspend fun deleteDailyLogById(id: Int) = withContext(Dispatchers.IO) {
        foodDao.deleteDailyLogById(id)
    }

    // Direct Gemini Scanning Core
    suspend fun scanFoodImage(bitmap: Bitmap, languageCode: String = "en", imagePathString: String? = null): FoodItem = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured. Please add it to the Secrets panel in AI Studio.")
        }

        // Convert Bitmap to Base64
        val base64Image = bitmap.toBase64()

        // Set prompt with requested language localization
        val langInstruction = when (languageCode) {
            "es" -> "Understand Spanish. All returned fields, names, descriptions, and insights must be translated and written in Spanish."
            "hi" -> "Understand Hindi. All returned fields, names, descriptions, and insights must be translated and written in Hindi."
            "fr" -> "Understand French. All returned fields, names, descriptions, and insights must be translated and written in French."
            "de" -> "Understand German. All returned fields, names, descriptions, and insights must be translated and written in German."
            else -> "Return all fields, names, descriptions, and insights in English."
        }

        val promptText = """
            You are NutriScan AI, an expert food scientist, biochemist, botanist, traditional herbalist, and nutritionist.
            Analyze the provided image and identify the primary edible item. It MUST be a fruit, vegetable, spice, herb, dry fruit, seed, or nut.
            
            $langInstruction
            
            Respond ONLY with a single JSON object conforming to this shape, with no formatting, markdown backticks, or wrapping. It must be valid JSON:
            {
              "name": "Common Name of the food item",
              "scientificName": "Scientific botanical genus and species name",
              "category": "Fruits" or "Vegetables" or "Spices & Herbs" or "Dry Fruits" or "Seeds" or "Nuts",
              "calories": 52.0,
              "protein": 0.3,
              "carbs": 13.8,
              "fats": 0.2,
              "fiber": 2.4,
              "potassium": "107 mg",
              "calcium": "6 mg",
              "iron": "0.1 mg",
              "magnesium": "5 mg",
              "zinc": "0.04 mg",
              "phosphorus": "11 mg",
              "vitamins": "Vitamin C: 4.6mg, Vitamin B6: 0.04mg",
              "tasteProfile": "Sweet and slightly acidic",
              "origin": "Central Asia / Kazakhstan",
              "season": "Autumn",
              "shelfLife": "2-4 weeks refrigerated",
              "ayurvedicProperties": "Pacifies Pitta and Vata, increases Kapha if overly sweet. Light and cooling.",
              "medicinalUses": "Supports colon health, lowers bad cholesterol, stabilizes blood sugar levels.",
              "healthBenefits": "High in antioxidant flavonoids, dietary fiber helps digest, reduces cardiovascular risks.",
              "precautions": "Wash thoroughly to remove surface pesticides. Seeds contain small amounts of amygdalin.",
              "allergies": "Oral allergy syndrome rare but possible in birch pollen allergy sufferers.",
              "recommendedDailyIntake": "1 medium-sized apple (approx 150-180g)",
              "glycemicIndex": "Low (around 38)",
              "sideEffects": "No major side effects. Excessive intake might cause mild digestive gas due to fiber.",
              "bestTimeToConsume": "Morning on empty stomach or as a midday snack.",
              "recommendedQuantity": "1-2 medium apples per day",
              "storageTips": "Store in a cool warehouse, fruit drawer of home refrigerator for up to 4 weeks.",
              "traditionalUses": "Traditionally used to tone liver and intestines, cleanse teeth, and provide sustained fluid energy."
            }

            CRITICAL: If the image does not clearly contain a fruit, vegetable, spice, herb, dry fruit, seed, or nut, return only this valid JSON object with the 'name' field as 'Unknown':
            {
              "name": "Unknown",
              "scientificName": "Not recognized",
              "category": "Fruits",
              "calories": 0.0,
              "protein": 0.0,
              "carbs": 0.0,
              "fats": 0.0,
              "fiber": 0.0,
              "potassium": "0 mg",
              "calcium": "0 mg",
              "iron": "0 mg",
              "magnesium": "0 mg",
              "zinc": "0 mg",
              "phosphorus": "0 mg",
              "vitamins": "N/A",
              "tasteProfile": "N/A",
              "origin": "Unknown",
              "season": "N/A",
              "shelfLife": "N/A",
              "ayurvedicProperties": "N/A",
              "medicinalUses": "N/A",
              "healthBenefits": "N/A",
              "precautions": "The scan was unable to distinctly identify a supported edible crop in the picture. Please ensure proper lighting, center the food item, and try scanning again.",
              "allergies": "N/A",
              "recommendedDailyIntake": "N/A"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Received an empty content response from Gemini API.")
            
            Log.d("NutriScanAI", "Received Gemini Response: $jsonText")

            // Clean markdown codeblocks if Gemini added them despite strict instruction
            val cleanedJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(FoodItemJson::class.java)
            val parsed = adapter.fromJson(cleanedJson)
                ?: throw IllegalStateException("Failed to parse Gemini output representation.")

            if (parsed.name == "Unknown") {
                return@withContext FoodItem(
                    name = parsed.name,
                    scientificName = parsed.scientificName ?: "Not recognized",
                    category = parsed.category ?: "Fruits",
                    calories = 0.0,
                    protein = 0.0,
                    carbs = 0.0,
                    fats = 0.0,
                    fiber = 0.0,
                    precautions = parsed.precautions ?: "Unrecognized item. Please position your item clearly in the center and try again.",
                    imageUri = imagePathString,
                    scannedAt = System.currentTimeMillis()
                )
            }

            val item = FoodItem(
                name = parsed.name,
                scientificName = parsed.scientificName ?: "Unknown",
                category = parsed.category ?: "Fruits",
                calories = parsed.calories ?: 0.0,
                protein = parsed.protein ?: 0.0,
                carbs = parsed.carbs ?: 0.0,
                fats = parsed.fats ?: 0.0,
                fiber = parsed.fiber ?: 0.0,
                potassium = parsed.potassium ?: "N/A",
                calcium = parsed.calcium ?: "N/A",
                iron = parsed.iron ?: "N/A",
                magnesium = parsed.magnesium ?: "N/A",
                zinc = parsed.zinc ?: "N/A",
                phosphorus = parsed.phosphorus ?: "N/A",
                vitamins = parsed.vitamins ?: "N/A",
                tasteProfile = parsed.tasteProfile ?: "N/A",
                origin = parsed.origin ?: "N/A",
                season = parsed.season ?: "N/A",
                shelfLife = parsed.shelfLife ?: "N/A",
                ayurvedicProperties = parsed.ayurvedicProperties ?: "N/A",
                medicinalUses = parsed.medicinalUses ?: "N/A",
                healthBenefits = parsed.healthBenefits ?: "N/A",
                precautions = parsed.precautions ?: "N/A",
                allergies = parsed.allergies ?: "None typical",
                recommendedDailyIntake = parsed.recommendedDailyIntake ?: "N/A",
                glycemicIndex = parsed.glycemicIndex ?: "N/A",
                sideEffects = parsed.sideEffects ?: "None known",
                bestTimeToConsume = parsed.bestTimeToConsume ?: "Anytime",
                recommendedQuantity = parsed.recommendedQuantity ?: "N/A",
                storageTips = parsed.storageTips ?: "N/A",
                traditionalUses = parsed.traditionalUses ?: "N/A",
                imageUri = imagePathString,
                scannedAt = System.currentTimeMillis()
            )

            // Cache it locally in the scan history database automatically
            val savedId = foodDao.insertFoodItem(item)
            item.copy(id = savedId.toInt())

        } catch (e: Exception) {
            Log.e("NutriScanAI", "Image scan fail", e)
            throw e
        }
    }

    // Direct Gemini Text lookup to find details for food by name
    suspend fun lookupFoodDetailsByName(name: String, languageCode: String = "en"): FoodItem = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured. Please add it to the Secrets panel in AI Studio.")
        }

        // Set prompt with requested language localization
        val langInstruction = when (languageCode) {
            "es" -> "Understand Spanish. All returned fields, names, descriptions, and insights must be translated and written in Spanish."
            "hi" -> "Understand Hindi. All returned fields, names, descriptions, and insights must be translated and written in Hindi."
            "fr" -> "Understand French. All returned fields, names, descriptions, and insights must be translated and written in French."
            "de" -> "Understand German. All returned fields, names, descriptions, and insights must be translated and written in German."
            else -> "Return all fields, names, descriptions, and insights in English."
        }

        val promptText = """
            You are NutriScan AI, an expert food scientist, biochemist, botanist, traditional herbalist, and nutritionist.
            Generate a detailed nutrition, botanical wellness, and traditional herbalist profile for the requested edible item: "$name".
            The item MUST be analyzed as a fruit, vegetable, spice, herb, dry fruit, seed, or nut.
            
            $langInstruction
            
            Respond ONLY with a single JSON object conforming to this shape, with no formatting, markdown backticks, or wrapping. It must be valid JSON:
            {
              "name": "Common Name of the food item",
              "scientificName": "Scientific botanical genus and species name",
              "category": "Fruits" or "Vegetables" or "Spices & Herbs" or "Dry Fruits" or "Seeds" or "Nuts",
              "calories": 52.0,
              "protein": 0.3,
              "carbs": 13.8,
              "fats": 0.2,
              "fiber": 2.4,
              "potassium": "107 mg",
              "calcium": "6 mg",
              "iron": "0.1 mg",
              "magnesium": "5 mg",
              "zinc": "0.04 mg",
              "phosphorus": "11 mg",
              "vitamins": "Vitamin C: 4.6mg, Vitamin B6: 0.04mg",
              "tasteProfile": "Sweet and slightly acidic",
              "origin": "Central Asia / Kazakhstan",
              "season": "Autumn",
              "shelfLife": "2-4 weeks refrigerated",
              "ayurvedicProperties": "Pacifies Pitta and Vata, increases Kapha if overly sweet. Light and cooling.",
              "medicinalUses": "Supports colon health, lowers bad cholesterol, stabilizes blood sugar levels.",
              "healthBenefits": "High in antioxidant flavonoids, dietary fiber helps digest, reduces cardiovascular risks.",
              "precautions": "Wash thoroughly to remove surface pesticides. Seeds contain small amounts of amygdalin.",
              "allergies": "Oral allergy syndrome rare but possible in birch pollen allergy sufferers.",
              "recommendedDailyIntake": "1 medium-sized apple (approx 150-180g)",
              "glycemicIndex": "Low (around 38)",
              "sideEffects": "No major side effects. Excessive intake might cause mild digestive gas due to fiber.",
              "bestTimeToConsume": "Morning on empty stomach or as a midday snack.",
              "recommendedQuantity": "1-2 medium apples per day",
              "storageTips": "Store in a cool warehouse, fruit drawer of home refrigerator for up to 4 weeks.",
              "traditionalUses": "Traditionally used to tone liver and intestines, cleanse teeth, and provide sustained fluid energy."
            }

            CRITICAL: If the input item "$name" is not a recognizable crop or is not an edible fruit, vegetable, spice, herb, seed, nut, or dry fruit, return only this valid JSON object with the 'name' field as 'Unknown':
            {
              "name": "Unknown",
              "scientificName": "Not recognized",
              "category": "Fruits",
              "calories": 0.0,
              "protein": 0.0,
              "carbs": 0.0,
              "fats": 0.0,
              "fiber": 0.0,
              "potassium": "0 mg",
              "calcium": "0 mg",
              "iron": "0 mg",
              "magnesium": "0 mg",
              "zinc": "0 mg",
              "phosphorus": "0 mg",
              "vitamins": "N/A",
              "tasteProfile": "N/A",
              "origin": "Unknown",
              "season": "N/A",
              "shelfLife": "N/A",
              "ayurvedicProperties": "N/A",
              "medicinalUses": "N/A",
              "healthBenefits": "N/A",
              "precautions": "The lookup was unable to distinctly identify a supported edible crop for '$name'. Please make sure it's a valid culinary fruit, vegetable, or spice.",
              "allergies": "N/A",
              "recommendedDailyIntake": "N/A"
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = promptText)
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("Received an empty content response from Gemini API.")
            
            Log.d("NutriScanAI", "Received Gemini Text Response: $jsonText")

            val cleanedJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = moshi.adapter(FoodItemJson::class.java)
            val parsed = adapter.fromJson(cleanedJson)
                ?: throw IllegalStateException("Failed to parse Gemini output representation.")

            if (parsed.name == "Unknown") {
                return@withContext FoodItem(
                    name = "Unknown Details",
                    scientificName = parsed.scientificName ?: "Not recognized",
                    category = parsed.category ?: "Fruits",
                    calories = 0.0,
                    protein = 0.0,
                    carbs = 0.0,
                    fats = 0.0,
                    fiber = 0.0,
                    precautions = parsed.precautions ?: "Unrecognized item. Please check the spelling or ask about a valid crop.",
                    scannedAt = System.currentTimeMillis()
                )
            }

            val item = FoodItem(
                name = parsed.name,
                scientificName = parsed.scientificName ?: "Unknown",
                category = parsed.category ?: "Fruits",
                calories = parsed.calories ?: 0.0,
                protein = parsed.protein ?: 0.0,
                carbs = parsed.carbs ?: 0.0,
                fats = parsed.fats ?: 0.0,
                fiber = parsed.fiber ?: 0.0,
                potassium = parsed.potassium ?: "N/A",
                calcium = parsed.calcium ?: "N/A",
                iron = parsed.iron ?: "N/A",
                magnesium = parsed.magnesium ?: "N/A",
                zinc = parsed.zinc ?: "N/A",
                phosphorus = parsed.phosphorus ?: "N/A",
                vitamins = parsed.vitamins ?: "N/A",
                tasteProfile = parsed.tasteProfile ?: "N/A",
                origin = parsed.origin ?: "N/A",
                season = parsed.season ?: "N/A",
                shelfLife = parsed.shelfLife ?: "N/A",
                ayurvedicProperties = parsed.ayurvedicProperties ?: "N/A",
                medicinalUses = parsed.medicinalUses ?: "N/A",
                healthBenefits = parsed.healthBenefits ?: "N/A",
                precautions = parsed.precautions ?: "N/A",
                allergies = parsed.allergies ?: "None typical",
                recommendedDailyIntake = parsed.recommendedDailyIntake ?: "N/A",
                glycemicIndex = parsed.glycemicIndex ?: "N/A",
                sideEffects = parsed.sideEffects ?: "None known",
                bestTimeToConsume = parsed.bestTimeToConsume ?: "Anytime",
                recommendedQuantity = parsed.recommendedQuantity ?: "N/A",
                storageTips = parsed.storageTips ?: "N/A",
                traditionalUses = parsed.traditionalUses ?: "N/A",
                scannedAt = System.currentTimeMillis()
            )

            // Cache it locally in the database history
            val savedId = foodDao.insertFoodItem(item)
            item.copy(id = savedId.toInt())

        } catch (e: Exception) {
            Log.e("NutriScanAI", "Text food details Lookup fail", e)
            throw e
        }
    }

    // Direct free-form chatbot response generator
    suspend fun askBotanicalAdvisor(question: String, languageCode: String = "en"): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Gemini API key is not configured. Please add it to the Secrets panel in AI Studio to use the AI chatbot."
        }

        val langInstruction = when (languageCode) {
            "es" -> "Understand Spanish. All responses must be detailed and written in Spanish."
            "hi" -> "Understand Hindi. All responses must be detailed and written in Hindi."
            "fr" -> "Understand French. All responses must be detailed and written in French."
            "de" -> "Understand German. All responses must be detailed and written in German."
            "te" -> "Understand Telugu. All responses must be detailed and written in Telugu."
            "mr" -> "Understand Marathi. All responses must be detailed and written in Marathi."
            "pa" -> "Understand Punjabi. All responses must be detailed and written in Punjabi."
            else -> "Return all responses in English."
        }

        val promptText = """
            You are NutriScan AI's expert Botanical Assistant, Biochemist, Traditional Herbalist, and Nutritionist.
            The user is asking: "$question".
            
            Provide an elegant, highly structured, and comprehensive response. Focus on foods, fruits, vegetables, herbs, spices, nuts, or seeds if related. 
            If the question asks about a specific crop/food, ensure you cover:
            1. 🍏 Nutrition Facts (Approximate calories, macros, fiber, key vitamins/minerals)
            2. 🌿 Health Benefits (Antioxidants, digestion, cardiac, metabolic benefits)
            3. 🧪 Medicinal Uses (Curing assistance, traditional usage, herbal remedies)
            4. ⚠️ Side Effects & Precautions (Toxicity, allergy, proper storage/washing)
            5. 🍽️ Recommended Daily Intake & Best Times to Consume
            
            $langInstruction
            
            Make sure your response has beautiful Markdown formatting with clear bullet points, bullet emojis, and clear section dividers so that it presents beautifully inside our Jetpack Compose text renderer.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = promptText))))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Received an empty response from Gemini BOT. Please modify the query and try again."
        } catch (e: Exception) {
            Log.e("NutriScanAI", "Chat advisor query failed", e)
            "Sorry, the AI Advisor is temporarily offline: " + e.localizedMessage
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    }

    // Query helper for daily tracking summary logic
    fun getFormattedDateToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

// Intermediate json entity to decode safe values
@JsonClass(generateAdapter = true)
data class FoodItemJson(
    val name: String,
    val scientificName: String?,
    val category: String?,
    val calories: Double?,
    val protein: Double?,
    val carbs: Double?,
    val fats: Double?,
    val fiber: Double?,
    val potassium: String?,
    val calcium: String?,
    val iron: String?,
    val magnesium: String?,
    val zinc: String?,
    val phosphorus: String?,
    val vitamins: String?,
    val tasteProfile: String?,
    val origin: String?,
    val season: String?,
    val shelfLife: String?,
    val ayurvedicProperties: String?,
    val medicinalUses: String?,
    val healthBenefits: String?,
    val precautions: String?,
    val allergies: String?,
    val recommendedDailyIntake: String?,
    val glycemicIndex: String?,
    val sideEffects: String?,
    val bestTimeToConsume: String?,
    val recommendedQuantity: String?,
    val storageTips: String?,
    val traditionalUses: String?
)
