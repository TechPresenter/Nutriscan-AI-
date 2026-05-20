package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.model.DailyNutritionLog
import com.example.data.model.FoodItem
import com.example.data.repository.FoodRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val foodItem: FoodItem) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = FoodRepository(db.foodDao())
    val settingsManager = SettingsManager(application)

    // Current State Flows from SharedPreferences
    val theme = settingsManager.theme
    val language = settingsManager.language
    val onboardingCompleted = settingsManager.onboardingCompleted
    val isAdFree = settingsManager.isAdFree

    // Goals Preferences
    val goalCalories = settingsManager.goalCalories
    val goalProtein = settingsManager.goalProtein
    val goalCarbs = settingsManager.goalCarbs
    val goalFats = settingsManager.goalFats

    // UI state for scans
    private val _scanUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanUiState: StateFlow<ScanUiState> = _scanUiState.asStateFlow()

    // UI state for direct text index lookups
    private val _lookupUiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val lookupUiState: StateFlow<ScanUiState> = _lookupUiState.asStateFlow()

    // History & Favorites flows
    val scanHistory: StateFlow<List<FoodItem>> = repository.scanHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FoodItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter results matching search
    val searchResults: StateFlow<List<FoodItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.scanHistory
            } else {
                repository.searchHistory(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Nutrition logs
    private val _currentDate = MutableStateFlow(repository.getFormattedDateToday())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    val dailyLogs: StateFlow<List<DailyNutritionLog>> = _currentDate
        .flatMapLatest { date -> repository.getDailyLogs(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly summary for charts (last 7 days)
    val weeklyLogs: StateFlow<List<DailyNutritionLog>> = repository.getLogsSince(
        System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Food Comparisons Selection
    var compareFoodA by mutableStateOf<FoodItem?>(null)
        private set
    var compareFoodB by mutableStateOf<FoodItem?>(null)
        private set

    // AdMob Monetization Triggers
    var scanCount by mutableStateOf(0)
        private set
    var shouldShowInterstitialAd by mutableStateOf(false)
    var activeNativeAdIndex by mutableStateOf(1) // Show ads periodically within the list items

    private val _simulatedInterstitialTrigger = MutableStateFlow(false)
    val simulatedInterstitialTrigger: StateFlow<Boolean> = _simulatedInterstitialTrigger.asStateFlow()

    fun dismissInterstitial() {
        shouldShowInterstitialAd = false
        _simulatedInterstitialTrigger.value = false
    }

    // AI Chat Botanical Assistant States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    fun sendChatMessage(text: String, startPrompt: String = "") {
        if (text.isBlank()) return
        val list = _chatMessages.value.toMutableList()
        list.add(ChatMessage(text = text, isUser = true))
        _chatMessages.value = list

        viewModelScope.launch {
            _chatLoading.value = true
            try {
                val fullQuery = if (startPrompt.isNotEmpty()) "$startPrompt. $text" else text
                val response = repository.askBotanicalAdvisor(fullQuery, language.value)
                val updatedList = _chatMessages.value.toMutableList()
                updatedList.add(ChatMessage(text = response, isUser = false))
                _chatMessages.value = updatedList
            } catch (e: Exception) {
                val updatedList = _chatMessages.value.toMutableList()
                updatedList.add(ChatMessage(text = "Sorry, I couldn't connect to the AI Advisor: ${e.message}", isUser = false))
                _chatMessages.value = updatedList
            } finally {
                _chatLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    init {
        Log.d("NutriScanAI", "NutriViewModel Initialized.")
        // Automatically pre-populate database on first-run so search works instantly
        viewModelScope.launch {
            try {
                // If "Golden Raisins" is not found, import/refresh the default database with the full 18 crop items
                val goldenRaisins = repository.getFoodItemByName("Golden Raisins")
                if (goldenRaisins == null) {
                    loadSampleData()
                }
            } catch (e: Exception) {
                Log.e("NutriScanAI", "Failed to auto-populate DB", e)
            }
        }
    }

    // --- Search Helper ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Settings helpers ---
    fun changeTheme(newTheme: String) {
        settingsManager.setTheme(newTheme)
    }

    fun setTheme(theme: String) {
        changeTheme(theme)
    }

    fun changeLanguage(newLangCode: String) {
        settingsManager.setLanguage(newLangCode)
    }

    fun setLanguage(lang: String) {
        changeLanguage(lang)
    }

    fun completeOnboarding() {
        settingsManager.setOnboardingCompleted(true)
    }

    fun updateNutritionGoals(calories: Float, protein: Float, carbs: Float, fats: Float) {
        settingsManager.setNutritionalGoals(calories, protein, carbs, fats)
    }

    fun setGoals(calories: Float, protein: Float, carbs: Float, fats: Float) {
        updateNutritionGoals(calories, protein, carbs, fats)
    }

    fun removeAds() {
        settingsManager.setAdFree(true)
    }

    fun setAdFree(adFree: Boolean) {
        viewModelScope.launch {
            settingsManager.setAdFree(adFree)
        }
    }

    // --- Scanner AI Actions ---
    fun scanImage(bitmap: Bitmap, imagePathString: String? = null) {
        viewModelScope.launch {
            _scanUiState.value = ScanUiState.Scanning
            try {
                val result = repository.scanFoodImage(bitmap, language.value, imagePathString)
                _scanUiState.value = ScanUiState.Success(result)
                
                // Keep track of scan counts for Interstitial Ads
                if (!isAdFree.value) {
                    scanCount++
                    if (scanCount % 2 == 0) { // Show ad screen every 2 successful scans for testing
                        shouldShowInterstitialAd = true
                        _simulatedInterstitialTrigger.value = true
                    }
                }
            } catch (e: Exception) {
                _scanUiState.value = ScanUiState.Error(
                    e.message ?: "An unexpected error occurred during image scan. Please review connection or key."
                )
            }
        }
    }

    fun clearScanState() {
        _scanUiState.value = ScanUiState.Idle
    }

    fun clearLookupUiState() {
        _lookupUiState.value = ScanUiState.Idle
    }

    fun lookupFoodDetailsByName(name: String, onFinished: (Int) -> Unit = {}) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _lookupUiState.value = ScanUiState.Scanning
            try {
                val result = repository.lookupFoodDetailsByName(name, language.value)
                _lookupUiState.value = ScanUiState.Success(result)
                onFinished(result.id)
            } catch (e: Exception) {
                _lookupUiState.value = ScanUiState.Error(
                    e.message ?: "An unexpected error occurred during direct lookup. Please verify spelling."
                )
            }
        }
    }

    // --- History & Favorites toggles ---
    fun toggleFavorite(foodItem: FoodItem) {
        viewModelScope.launch {
            val updated = foodItem.copy(isFavorite = !foodItem.isFavorite)
            repository.updateFoodItem(updated)
            
            // Sync active compare states if matches
            if (compareFoodA?.id == foodItem.id) compareFoodA = updated
            if (compareFoodB?.id == foodItem.id) compareFoodB = updated
            
            // If active success state, sync that as well
            val current = _scanUiState.value
            if (current is ScanUiState.Success && current.foodItem.id == foodItem.id) {
                _scanUiState.value = ScanUiState.Success(updated)
            }
        }
    }

    fun deleteFoodItem(foodItem: FoodItem) {
        viewModelScope.launch {
            repository.deleteFoodItem(foodItem)
            if (compareFoodA?.id == foodItem.id) compareFoodA = null
            if (compareFoodB?.id == foodItem.id) compareFoodB = null
        }
    }

    // --- Daily Logging Helper actions ---
    fun logFoodConsumption(foodItem: FoodItem, quantityGrams: Double) {
        viewModelScope.launch {
            val multiplier = quantityGrams / 100.0
            val log = DailyNutritionLog(
                date = _currentDate.value,
                foodName = foodItem.name,
                quantityGrams = quantityGrams,
                calories = foodItem.calories * multiplier,
                protein = foodItem.protein * multiplier,
                carbs = foodItem.carbs * multiplier,
                fats = foodItem.fats * multiplier
            )
            repository.insertDailyLog(log)
        }
    }

    fun deleteLog(log: DailyNutritionLog) {
        viewModelScope.launch {
            repository.deleteDailyLog(log)
        }
    }

    fun changeDateOffset(offsetDays: Int) {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val parsedDate = sdf.parse(_currentDate.value)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (e: Exception) {
            calendar.time = Date()
        }
        calendar.add(Calendar.DAY_OF_YEAR, offsetDays)
        _currentDate.value = sdf.format(calendar.time)
    }

    fun setDateToToday() {
        _currentDate.value = repository.getFormattedDateToday()
    }

    // --- Food Comparison Helpers ---
    fun selectForComparison(food: FoodItem, index: Int) {
        if (index == 0) {
            compareFoodA = food
        } else {
            compareFoodB = food
        }
    }

    fun clearComparison(index: Int) {
        if (index == 0) {
            compareFoodA = null
        } else {
            compareFoodB = null
        }
    }

    fun clearAllComparisons() {
        compareFoodA = null
        compareFoodB = null
    }

    // --- Quick Mock Generator (For demo purposes when API keys aren't added) ---
    fun loadSampleData() {
        viewModelScope.launch {
            val apple = FoodItem(
                name = "Red Gala Apple",
                scientificName = "Malus domestica",
                category = "Fruits",
                calories = 52.0,
                protein = 0.3,
                carbs = 13.8,
                fats = 0.2,
                fiber = 2.4,
                potassium = "107 mg",
                calcium = "6 mg",
                iron = "0.1 mg",
                magnesium = "5 mg",
                zinc = "0.04 mg",
                phosphorus = "11 mg",
                vitamins = "Vitamin C, Vitamin A, Vitamin B6",
                tasteProfile = "Crisp, sweet, floral",
                origin = "Central Asia",
                season = "August to Autumn",
                shelfLife = "1-2 weeks pantry, 1-2 months fridge",
                ayurvedicProperties = "Balances Vata & Pitta",
                medicinalUses = "Supports heart health & digestion",
                healthBenefits = "Rich in dietary fiber & antioxidant compounds",
                precautions = "Avoid eating central core seeds",
                allergies = "Generally safe",
                recommendedDailyIntake = "1 item (~180g)",
                isFavorite = true
            )

            val banana = FoodItem(
                name = "Cavendish Banana",
                scientificName = "Musa acuminata",
                category = "Fruits",
                calories = 89.0,
                protein = 1.1,
                carbs = 22.8,
                fats = 0.3,
                fiber = 2.6,
                potassium = "358 mg",
                calcium = "5 mg",
                iron = "0.3 mg",
                magnesium = "27 mg",
                zinc = "0.15 mg",
                phosphorus = "22 mg",
                vitamins = "Vitamin C, Vitamin B6, Folate",
                tasteProfile = "Creamy, sweet, uniform",
                origin = "Southeast Asia",
                season = "Year-round",
                shelfLife = "3-7 days at room temperature",
                ayurvedicProperties = "Nourishing, pacifies Vata & Pitta",
                medicinalUses = "Combats dehydration & hyperacidity",
                healthBenefits = "Excellent potassium source for blood pressure regulatory balance",
                precautions = "Avoid eating on cold empty stomach if prone to respiratory build-up",
                allergies = "Amylase food allergy rare",
                recommendedDailyIntake = "1-2 items daily",
                isFavorite = false
            )

            val orange = FoodItem(
                name = "Sweet Orange",
                scientificName = "Citrus sinensis",
                category = "Fruits",
                calories = 47.0,
                protein = 0.9,
                carbs = 11.8,
                fats = 0.1,
                fiber = 2.4,
                potassium = "181 mg",
                calcium = "40 mg",
                iron = "0.1 mg",
                magnesium = "10 mg",
                zinc = "0.07 mg",
                phosphorus = "14 mg",
                vitamins = "Vitamin C, Folate, Thiamine",
                tasteProfile = "Tart-sweet, burst of citrus fragrance",
                origin = "Southern China & India region",
                season = "Late autumn to spring harvest",
                shelfLife = "1 week at room temp, 2-3 weeks in fridge",
                ayurvedicProperties = "Pacifies Vata, slightly increases Pitta",
                medicinalUses = "Flesh boosts immune white cells, rind oils relieve flatulence",
                healthBenefits = "Extraordinary Vitamin C density prevents systemic cellular damage",
                precautions = "Avoid drinking sour juice with dairy milk directly",
                allergies = "Citrus sensitivities may occur",
                recommendedDailyIntake = "1-2 oranges daily",
                isFavorite = false
            )

            val blueberries = FoodItem(
                name = "Wild Blueberries",
                scientificName = "Vaccinium corymbosum",
                category = "Fruits",
                calories = 57.0,
                protein = 0.7,
                carbs = 14.5,
                fats = 0.3,
                fiber = 2.4,
                potassium = "77 mg",
                calcium = "6 mg",
                iron = "0.3 mg",
                magnesium = "6 mg",
                zinc = "0.16 mg",
                phosphorus = "12 mg",
                vitamins = "Vitamin C, Vitamin K",
                tasteProfile = "Tangy, intensely sweet berry profile",
                origin = "North America",
                season = "Late spring to summer",
                shelfLife = "5-10 days kept cool",
                ayurvedicProperties = "Slightly cooling, pacifies Pitta & Kapha",
                medicinalUses = "Improves microvascular flow and general eyesight health",
                healthBenefits = "Extremely dense list of anthocyanin pigments targeting free radicals",
                precautions = "Wash well to clear surface fields wild yeast",
                allergies = "Very low allergen rating",
                recommendedDailyIntake = "1 cup (~150g)",
                isFavorite = false
            )

            val broccoli = FoodItem(
                name = "Green Broccoli",
                scientificName = "Brassica oleracea var. italica",
                category = "Vegetables",
                calories = 34.0,
                protein = 2.8,
                carbs = 7.0,
                fats = 0.4,
                fiber = 2.6,
                potassium = "316 mg",
                calcium = "47 mg",
                iron = "0.7 mg",
                magnesium = "21 mg",
                zinc = "0.4 mg",
                phosphorus = "66 mg",
                vitamins = "Vitamin C: 89.2 mg, Vitamin K: 101.6 mcg, Folate: 63 mcg",
                tasteProfile = "Earthy, slightly bittersweet stalks",
                origin = "Mediterranean basin, Ancient Rome",
                season = "Cool seasons, autumn to early spring",
                shelfLife = "5-7 days refrigerated in aerated bag",
                ayurvedicProperties = "Increases Vata due to airiness. Light, dry, and cooling. Best cooked.",
                medicinalUses = "Potent cancer-preventive compounds, stimulates cellular detox pathway triggers.",
                healthBenefits = "Incredible glucosinolates neutralize damaging free radicals and aid liver filter functions.",
                precautions = "High Vitamin K content slightly reduces blood thinning medication efficacies.",
                allergies = "Generally safe. Excess portion can cause gas/bloat in sensitive digestion.",
                recommendedDailyIntake = "1 cup chopped (~90-100g)",
                isFavorite = false
            )

            val carrot = FoodItem(
                name = "Organic Carrots",
                scientificName = "Daucus carota",
                category = "Vegetables",
                calories = 41.0,
                protein = 0.9,
                carbs = 9.6,
                fats = 0.2,
                fiber = 2.8,
                potassium = "320 mg",
                calcium = "33 mg",
                iron = "0.3 mg",
                magnesium = "12 mg",
                zinc = "0.24 mg",
                phosphorus = "35 mg",
                vitamins = "Vitamin A, Vitamin K1, Vitamin B6",
                tasteProfile = "Sweet, woody, deeply crunchy",
                origin = "Persia & Asia Minor",
                season = "Year-round, peak in early summer",
                shelfLife = "3-4 weeks in refrigerator drawer",
                ayurvedicProperties = "Pacifies Vata & Kapha, increases Pitta if raw",
                medicinalUses = "Supports retinal rod function and cleanses gut walls",
                healthBenefits = "Abundantly high in beta-carotene which converts to Vitamin A in liver",
                precautions = "Excess intake might turn skin palms slightly orange-yellow safely",
                allergies = "Incredibly rare",
                recommendedDailyIntake = "1-2 medium carrots daily",
                isFavorite = false
            )

            val spinach = FoodItem(
                name = "Organic Spinach",
                scientificName = "Spinacia oleracea",
                category = "Vegetables",
                calories = 23.0,
                protein = 2.9,
                carbs = 3.6,
                fats = 0.4,
                fiber = 2.2,
                potassium = "558 mg",
                calcium = "99 mg",
                iron = "2.7 mg",
                magnesium = "79 mg",
                zinc = "0.53 mg",
                phosphorus = "49 mg",
                vitamins = "Vitamin A, Vitamin C, Vitamin K",
                tasteProfile = "Slightly metallic, clean green leaf",
                origin = "Ancient Persia",
                season = "Spring & Autumn cold frames",
                shelfLife = "3-5 days refrigerated",
                ayurvedicProperties = "Cooling and dry, balances Pitta",
                medicinalUses = "Prevents general anemia & structural bone calcification failures",
                healthBenefits = "High level of lutein protects macular structures in eyes",
                precautions = "Contains oxalates; cook thoroughly to offset stone risks",
                allergies = "Low allergen rating",
                recommendedDailyIntake = "1 cup cooked (~150g)",
                isFavorite = false
            )

            val tomato = FoodItem(
                name = "Red Vine Tomato",
                scientificName = "Solanum lycopersicum",
                category = "Vegetables",
                calories = 18.0,
                protein = 0.9,
                carbs = 3.9,
                fats = 0.2,
                fiber = 1.2,
                potassium = "237 mg",
                calcium = "10 mg",
                iron = "0.3 mg",
                magnesium = "11 mg",
                zinc = "0.17 mg",
                phosphorus = "24 mg",
                vitamins = "Vitamin C, Lycopene, Beta-Carotene",
                tasteProfile = "Juicy, savory-sweet umami",
                origin = "South American Andes",
                season = "Summer sun ripened",
                shelfLife = "5-7 days at room temperature (do not freeze)",
                ayurvedicProperties = "Heats body, pacifies Vata, slightly raises Pitta",
                medicinalUses = "Nourishes the blood cells & prevents premature sun burning of outer dermis",
                healthBenefits = "Vast reserves of lycopene which becomes even more bioavailable when cooked",
                precautions = "Avoid green unripe stems which contain trace solanine alkaloids",
                allergies = "Nightshade allergen potential is present in sensitive people",
                recommendedDailyIntake = "1-2 medium tomatoes daily",
                isFavorite = false
            )

            val basil = FoodItem(
                name = "Fresh Sweet Basil",
                scientificName = "Ocimum basilicum",
                category = "Spices & Herbs",
                calories = 23.0,
                protein = 3.2,
                carbs = 2.7,
                fats = 0.6,
                fiber = 1.6,
                potassium = "295 mg",
                calcium = "177 mg",
                iron = "3.2 mg",
                magnesium = "64 mg",
                zinc = "0.81 mg",
                phosphorus = "56 mg",
                vitamins = "Vitamin K, Vitamin A, Linalool",
                tasteProfile = "Aromatic, warming, hints of anise",
                origin = "India & tropical Asia",
                season = "Warm summer",
                shelfLife = "3-5 days in water cup at room temp",
                ayurvedicProperties = "Warm, dry, clears lung Kapha & balances Vata",
                medicinalUses = "Alleviates bloating, fever & clears stress hormones",
                healthBenefits = "Essential oils trigger body stress reduction & support gut microflora",
                precautions = "Use fresh cuttings; over-dried leaves lose aromatic potency",
                allergies = "Generally safe",
                recommendedDailyIntake = "5-10 fresh leaves daily",
                isFavorite = false
            )

            val mint = FoodItem(
                name = "Peppermint Leaves",
                scientificName = "Mentha piperita",
                category = "Spices & Herbs",
                calories = 70.0,
                protein = 3.8,
                carbs = 14.9,
                fats = 0.9,
                fiber = 8.0,
                potassium = "569 mg",
                calcium = "243 mg",
                iron = "5.1 mg",
                magnesium = "80 mg",
                zinc = "1.11 mg",
                phosphorus = "73 mg",
                vitamins = "Vitamin A, Menthol, Vitamin C",
                tasteProfile = "Brisk, coolant sensation, mentholated",
                origin = "Europe & Middle East",
                season = "Spring to late autumn",
                shelfLife = "5-7 days refrigerated in damp towel",
                ayurvedicProperties = "Cooling, uniquely pacifies all three doshas",
                medicinalUses = "Relieves severe abdominal spasms & throat mucus",
                healthBenefits = "Actively improves bowel function & reduces travel sickness",
                precautions = "Excellent, but avoid over-concentration if struggling with acid reflux",
                allergies = "Extremely low incidence",
                recommendedDailyIntake = "1 small sprig or tea brew daily",
                isFavorite = false
            )

            val cilantro = FoodItem(
                name = "Fresh Coriander Cilantro",
                scientificName = "Coriandrum sativum",
                category = "Spices & Herbs",
                calories = 23.0,
                protein = 2.1,
                carbs = 3.7,
                fats = 0.5,
                fiber = 2.8,
                potassium = "521 mg",
                calcium = "67 mg",
                iron = "1.8 mg",
                magnesium = "26 mg",
                zinc = "0.5 mg",
                phosphorus = "48 mg",
                vitamins = "Vitamin K, Vitamin A, Vitamin C",
                tasteProfile = "Bright, citrus-like green herb",
                origin = "Southern Europe & North Africa",
                season = "Cool spring & fall seasons",
                shelfLife = "5-9 days in water vase in fridge",
                ayurvedicProperties = "Sweet and cooling, pacifies Pitta",
                medicinalUses = "Aids toxic heavy metal detox & reduces kidney puffiness",
                healthBenefits = "Cineole elements actively reduce arthritic swelling & stimulate digestion",
                precautions = "Wash thoroughly to eliminate farm dust deposits in folds",
                allergies = "Small population matches taste with soapy aldehydes",
                recommendedDailyIntake = "1/2 cup chopped (~20g)",
                isFavorite = false
            )

            val turmeric = FoodItem(
                name = "Ground Yellow Turmeric",
                scientificName = "Curcuma longa",
                category = "Spices & Herbs",
                calories = 312.0,
                protein = 9.7,
                carbs = 67.1,
                fats = 3.3,
                fiber = 22.7,
                potassium = "2080 mg",
                calcium = "168 mg",
                iron = "55.0 mg",
                magnesium = "193 mg",
                zinc = "4.5 mg",
                phosphorus = "299 mg",
                vitamins = "Curcuminoids, Vitamin B6, Iron",
                tasteProfile = "Earthy, warm, bitter-sweet woody note",
                origin = "Indian Subcontinent",
                season = "Harvested in dry winter",
                shelfLife = "1-2 years sealed in dark pantry",
                ayurvedicProperties = "Bitter, warm, purifies blood, pacifies Kapha",
                medicinalUses = "Powerful antiseptic, wound healer & joint support resource",
                healthBenefits = "Curcumin is a highly researched anti-inflammatory compound",
                precautions = "Combine with pinch of black pepper to multiply curcumin absorption 2000%",
                allergies = "Slight contact sensitivity rare",
                recommendedDailyIntake = "1/2 to 1 teaspoon daily",
                isFavorite = false
            )

            val ginger = FoodItem(
                name = "Fresh Ginger Root",
                scientificName = "Zingiber officinale",
                category = "Spices & Herbs",
                calories = 80.0,
                protein = 1.8,
                carbs = 17.8,
                fats = 0.8,
                fiber = 2.0,
                potassium = "415 mg",
                calcium = "16 mg",
                iron = "0.6 mg",
                magnesium = "43 mg",
                zinc = "0.34 mg",
                phosphorus = "34 mg",
                vitamins = "Gingerols, Shogaols, Vitamin C",
                tasteProfile = "Spicy, sweet, highly warming and pungent",
                origin = "Maritime Southeast Asia",
                season = "Autumn root harvest",
                shelfLife = "2-3 weeks in fridge drawer, 6 months frozen",
                ayurvedicProperties = "Deeply stokes digestive fire, pacifies Vata & Kapha",
                medicinalUses = "Prevents motion sickness, halts nausea & throat inflammation",
                healthBenefits = "Accelerates gastric emptying & relieves muscular soreness",
                precautions = "Minimize dosage prior to major invasive dental procedures",
                allergies = "Generally safe",
                recommendedDailyIntake = "1-2 cm piece sliced daily",
                isFavorite = false
            )

            val chia = FoodItem(
                name = "Organic Chia Seeds",
                scientificName = "Salvia hispanica",
                category = "Seeds",
                calories = 486.0,
                protein = 16.5,
                carbs = 42.1,
                fats = 30.7,
                fiber = 34.4,
                potassium = "407 mg",
                calcium = "631 mg",
                iron = "7.7 mg",
                magnesium = "335 mg",
                zinc = "4.6 mg",
                phosphorus = "860 mg",
                vitamins = "Vitamin E, Thiamine, Niacin",
                tasteProfile = "Extremely mild, nutty, highly gelled when soaked",
                origin = "Central America & Mexico",
                season = "Harvested once dry in autumn",
                shelfLife = "1-3 years in sealed containers",
                ayurvedicProperties = "Heavily nourishing, builds tissues, pacifies Vata",
                medicinalUses = "Assists constipation clearance by supplying massive bulking mucilage",
                healthBenefits = "Superlative plant concentration of heart-protective omega-3 line",
                precautions = "Always pre-soak in water for 15 minutes before consuming",
                allergies = "Low allergen incident rating",
                recommendedDailyIntake = "1-2 tablespoons (approx 15-20g)",
                isFavorite = false
            )

            val flax = FoodItem(
                name = "Golden Flax Seeds",
                scientificName = "Linum usitatissimum",
                category = "Seeds",
                calories = 534.0,
                protein = 18.3,
                carbs = 28.9,
                fats = 42.2,
                fiber = 27.3,
                potassium = "813 mg",
                calcium = "255 mg",
                iron = "5.7 mg",
                magnesium = "392 mg",
                zinc = "4.3 mg",
                phosphorus = "642 mg",
                vitamins = "Vitamin B1, Lignans, Vitamin E",
                tasteProfile = "Toasty, nutty, rich seed coat",
                origin = "Middle East to Mediterranean",
                season = "Late summer crop",
                shelfLife = "1 year whole seeds, 2-3 months ground",
                ayurvedicProperties = "Pacifies Vata, slightly increases Kapha",
                medicinalUses = "Regulates endocrine cycle balances with phytoestrogens",
                healthBenefits = "Unmatched levels of gut lignans which improve digestive safety",
                precautions = "Must be consumed ground; whole seeds pass through gut fully undigested",
                allergies = "Very rare",
                recommendedDailyIntake = "1-2 tablespoons daily",
                isFavorite = false
            )

            val almonds = FoodItem(
                name = "Raw California Almonds",
                scientificName = "Prunus dulcis",
                category = "Nuts",
                calories = 579.0,
                protein = 21.2,
                carbs = 21.6,
                fats = 49.9,
                fiber = 12.5,
                potassium = "733 mg",
                calcium = "269 mg",
                iron = "3.7 mg",
                magnesium = "270 mg",
                zinc = "3.1 mg",
                phosphorus = "481 mg",
                vitamins = "Vitamin E: 25.6 mg, Riboflavin: 1.1 mg",
                tasteProfile = "Mild, sweet, and incredibly rich-nutty",
                origin = "Middle East, South Asia",
                season = "Late summer crop",
                shelfLife = "6-12 months sealed dry place",
                ayurvedicProperties = "Sparsely increases Kapha. Heavily pacifies Vata & Pitta. Highly nourishing.",
                medicinalUses = "Brain tissue cognitive boosters, hair growth enhancement, cholesterol reduction.",
                healthBenefits = "High level protective Vitamin E prevents oxidative stress on vital vascular walls.",
                precautions = "Rinse or soak before eating to soften outer seed coat enzyme inhibitors.",
                allergies = "Tree nut allergen. Avoid entirely if a diagnosed nut allergy exists.",
                recommendedDailyIntake = "1 ounce (~23 nuts, 28g)",
                isFavorite = false
            )

            val walnuts = FoodItem(
                name = "English Walnuts",
                scientificName = "Juglans regia",
                category = "Nuts",
                calories = 654.0,
                protein = 15.2,
                carbs = 13.7,
                fats = 65.2,
                fiber = 6.7,
                potassium = "441 mg",
                calcium = "98 mg",
                iron = "2.9 mg",
                magnesium = "158 mg",
                zinc = "3.1 mg",
                phosphorus = "346 mg",
                vitamins = "Vitamin B6, Folate, Vitamin E",
                tasteProfile = "Mild, rich, slightly bitter skin notes",
                origin = "Persia & Central Asia",
                season = "Autumn fall harvest",
                shelfLife = "6 months in shell, refrigerate once opened",
                ayurvedicProperties = "Warms system, pacifies Vata, raises Kapha",
                medicinalUses = "Supports neural signaling networks & cardiac artery elasticity",
                healthBenefits = "Highest nut concentration of health-promoting alpha-linolenic acid (ALA)",
                precautions = "Avoid eating if rancid; oils decompose quickly in high summer humidity",
                allergies = "Tree nut allergen status is highly prominent",
                recommendedDailyIntake = "7 whole shelled walnut halves daily (~28g)",
                isFavorite = false
            )

            val raisins = FoodItem(
                name = "Golden Raisins",
                scientificName = "Vitis vinifera",
                category = "Dry Fruits",
                calories = 299.0,
                protein = 3.3,
                carbs = 79.2,
                fats = 0.5,
                fiber = 4.5,
                potassium = "749 mg",
                calcium = "62 mg",
                iron = "1.8 mg",
                magnesium = "36 mg",
                zinc = "0.22 mg",
                phosphorus = "101 mg",
                vitamins = "Vitamin B6, Iron, Potassium",
                tasteProfile = "Intensely sweet, chewy with a rich fruity tang",
                origin = "Persia & Egypt",
                season = "Late summer dry suns",
                shelfLife = "12 months kept in low humidity sealed zip-locks",
                ayurvedicProperties = "Moist, cooling, deeply restorative, balances Vata & Pitta",
                medicinalUses = "Combats dry respiratory coughs & naturally boosts red hematocrite counts",
                healthBenefits = "Concentrated quick carbohydrate and iron reserves help clear chronic exhaustion",
                precautions = "Clean residues; dry sulfur compounds are sometimes used to keep colors golden",
                allergies = "Generally safe",
                recommendedDailyIntake = "1 small handful (~30-40g)",
                isFavorite = false
            )

            repository.insertFoodItem(apple)
            repository.insertFoodItem(banana)
            repository.insertFoodItem(orange)
            repository.insertFoodItem(blueberries)
            repository.insertFoodItem(broccoli)
            repository.insertFoodItem(carrot)
            repository.insertFoodItem(spinach)
            repository.insertFoodItem(tomato)
            repository.insertFoodItem(basil)
            repository.insertFoodItem(mint)
            repository.insertFoodItem(cilantro)
            repository.insertFoodItem(turmeric)
            repository.insertFoodItem(ginger)
            repository.insertFoodItem(chia)
            repository.insertFoodItem(flax)
            repository.insertFoodItem(almonds)
            repository.insertFoodItem(walnuts)
            repository.insertFoodItem(raisins)
        }
    }
}
