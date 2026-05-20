package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nutriscan_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "theme_preference" // "dark", "light", "system"
        private const val KEY_LANGUAGE = "language_code"  // "en", "es", "hi", "fr", "de"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_GOAL_CALORIES = "goal_calories"
        private const val KEY_GOAL_PROTEIN = "goal_protein"
        private const val KEY_GOAL_CARBS = "goal_carbs"
        private const val KEY_GOAL_FATS = "goal_fats"
        private const val KEY_PREMIUM = "is_ad_free"
    }

    private val _theme = MutableStateFlow(prefs.getString(KEY_THEME, "system") ?: "system")
    val theme: StateFlow<String> = _theme

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted

    private val _goalCalories = MutableStateFlow(prefs.getFloat(KEY_GOAL_CALORIES, 2000f))
    val goalCalories: StateFlow<Float> = _goalCalories

    private val _goalProtein = MutableStateFlow(prefs.getFloat(KEY_GOAL_PROTEIN, 60f))
    val goalProtein: StateFlow<Float> = _goalProtein

    private val _goalCarbs = MutableStateFlow(prefs.getFloat(KEY_GOAL_CARBS, 250f))
    val goalCarbs: StateFlow<Float> = _goalCarbs

    private val _goalFats = MutableStateFlow(prefs.getFloat(KEY_GOAL_FATS, 65f))
    val goalFats: StateFlow<Float> = _goalFats

    private val _isAdFree = MutableStateFlow(prefs.getBoolean(KEY_PREMIUM, false))
    val isAdFree: StateFlow<Boolean> = _isAdFree

    fun setTheme(value: String) {
        prefs.edit().putString(KEY_THEME, value).apply()
        _theme.value = value
    }

    fun setLanguage(value: String) {
        prefs.edit().putString(KEY_LANGUAGE, value).apply()
        _language.value = value
    }

    fun setOnboardingCompleted(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()
        _onboardingCompleted.value = value
    }

    fun setNutritionalGoals(energy: Float, protein: Float, carbs: Float, fats: Float) {
        prefs.edit()
            .putFloat(KEY_GOAL_CALORIES, energy)
            .putFloat(KEY_GOAL_PROTEIN, protein)
            .putFloat(KEY_GOAL_CARBS, carbs)
            .putFloat(KEY_GOAL_FATS, fats)
            .apply()
        _goalCalories.value = energy
        _goalProtein.value = protein
        _goalCarbs.value = carbs
        _goalFats.value = fats
    }

    fun setAdFree(value: Boolean) {
        prefs.edit().putBoolean(KEY_PREMIUM, value).apply()
        _isAdFree.value = value
    }
}
