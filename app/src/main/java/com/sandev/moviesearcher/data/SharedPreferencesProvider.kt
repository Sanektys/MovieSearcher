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

    init {
        if (sharedPreferences.getBoolean(keyFirstLaunch, true)) {
            sharedPreferences.edit { putBoolean(keyFirstLaunch, false) }
            sharedPreferences.edit { putBoolean(KEY_SHOW_SPLASH_SCREEN, isSplashScreenActivatedByDefault) }
            sharedPreferences.edit { putString(KEY_CATEGORY, defaultCategory) }
        }
    }


    fun setDefaultCategory(category: String) = sharedPreferences.edit { putString(KEY_CATEGORY, category) }

    fun getDefaultCategory() = sharedPreferences.getString(KEY_CATEGORY, defaultCategory)!!

    fun setShowingSplashScreen(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(KEY_SHOW_SPLASH_SCREEN, isEnabled)
    }

    fun isSplashScreenEnabled() = sharedPreferences.getBoolean(KEY_SHOW_SPLASH_SCREEN, isSplashScreenActivatedByDefault)

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
    }
}