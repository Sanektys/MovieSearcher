package com.sandev.moviesearcher.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R


class SharedPreferencesProvider(context: Context) {

    private val appContext = context.applicationContext

    private val sharedPreferences = appContext.getSharedPreferences(
        appContext.getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE)

    private val keyFirstLaunch = appContext.getString(R.string.shared_preferences_settings_key_first_launch)
    private val defaultCategory = appContext.getString(R.string.shared_preferences_settings_default_category)

    private val isSplashScreenActivatedByDefault = true
    private val isRatingDonutAnimationEnabledByDefault = true

    private val isSplashScreenSwitchButtonEnabledByDefault = true
    private val isRatingDonutSwitchButtonEnabledByDefault = true

    init {
        if (sharedPreferences.getBoolean(keyFirstLaunch, true)) {
            sharedPreferences.edit { putBoolean(keyFirstLaunch, false) }
            sharedPreferences.edit { putBoolean(KEY_SHOW_SPLASH_SCREEN, isSplashScreenActivatedByDefault) }
            sharedPreferences.edit { putString(KEY_CATEGORY, defaultCategory) }
            sharedPreferences.edit { putBoolean(KEY_ENABLE_RATING_DONUT_ANIMATION, isRatingDonutAnimationEnabledByDefault)}
            sharedPreferences.edit { putBoolean(KEY_ENABLE_SPLASH_SCREEN_SWITCH_BUTTON, isSplashScreenSwitchButtonEnabledByDefault)}
            sharedPreferences.edit { putBoolean(KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON, isRatingDonutSwitchButtonEnabledByDefault)}
        }
    }


    fun setDefaultCategory(category: String) = sharedPreferences.edit { putString(KEY_CATEGORY, category) }

    fun getDefaultCategory() = sharedPreferences.getString(KEY_CATEGORY, defaultCategory)!!

    fun setShowingSplashScreen(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(KEY_SHOW_SPLASH_SCREEN, isEnabled)
    }

    fun isSplashScreenEnabled() = sharedPreferences.getBoolean(KEY_SHOW_SPLASH_SCREEN, isSplashScreenActivatedByDefault)

    fun setRatingDonutAnimationState(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(KEY_ENABLE_RATING_DONUT_ANIMATION, isEnabled)
    }

    fun isRatingDonutAnimationEnabled() = sharedPreferences.getBoolean(
        KEY_ENABLE_RATING_DONUT_ANIMATION, isRatingDonutAnimationEnabledByDefault
    )

    fun setSplashScreenSwitchButtonState(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(KEY_ENABLE_SPLASH_SCREEN_SWITCH_BUTTON, isEnabled)
    }

    fun isSplashScreenSwitchButtonEnabled() = sharedPreferences.getBoolean(
        KEY_ENABLE_SPLASH_SCREEN_SWITCH_BUTTON, isSplashScreenSwitchButtonEnabledByDefault
    )

    fun setRatingDonutSwitchButtonState(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON, isEnabled)
    }

    fun isRatingDonutSwitchButtonEnabled() = sharedPreferences.getBoolean(
        KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON, isRatingDonutSwitchButtonEnabledByDefault
    )

    fun registerSharedPreferencesChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)

    fun unregisterSharedPreferencesChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)


    companion object {
        val KEY_CATEGORY = App.instance.getString(R.string.shared_preferences_settings_key_category)

        val CATEGORY_POPULAR  = App.instance.getString(R.string.shared_preferences_settings_value_category_popular)
        val CATEGORY_TOP      = App.instance.getString(R.string.shared_preferences_settings_value_category_top_rated)
        val CATEGORY_UPCOMING = App.instance.getString(R.string.shared_preferences_settings_value_category_upcoming)
        val CATEGORY_PLAYING  = App.instance.getString(R.string.shared_preferences_settings_value_category_now_playing)

        val KEY_SHOW_SPLASH_SCREEN = App.instance.getString(R.string.shared_preferences_settings_key_show_splash_screen)

        val KEY_ENABLE_RATING_DONUT_ANIMATION = App.instance.getString(R.string.shared_preferences_settings_key_enable_rating_donut_animation)

        val KEY_ENABLE_SPLASH_SCREEN_SWITCH_BUTTON = App.instance.getString(R.string.shared_preferences_settings_key_splash_screen_switch_button_enabled)
        val KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON = App.instance.getString(R.string.shared_preferences_settings_key_rating_donut_switch_button_enabled)
    }
}