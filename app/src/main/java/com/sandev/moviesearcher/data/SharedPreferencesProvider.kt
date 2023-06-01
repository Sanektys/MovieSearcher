package com.sandev.moviesearcher.data

import android.content.Context
import androidx.core.content.edit
import com.sandev.moviesearcher.R


class SharedPreferencesProvider(context: Context) {

    private val appContext = context.applicationContext

    private val sharedPreferences = appContext.getSharedPreferences(
        appContext.getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE)

    private val keyFirstLaunch = appContext.getString(R.string.shared_preferences_settings_key_first_launch)
    private val keyCategory = appContext.getString(R.string.shared_preferences_settings_key_category)

    private val defaultCategory = appContext.getString(R.string.shared_preferences_settings_default_category)

    init {
        if (sharedPreferences.getBoolean(keyFirstLaunch, true)) {
            sharedPreferences.edit { putBoolean(keyFirstLaunch, false) }
            sharedPreferences.edit { putString(keyCategory, defaultCategory) }
        }
    }


    fun setDefaultCategory(category: String) = sharedPreferences.edit { putString(keyCategory, category) }

    fun getDefaultCategory() = sharedPreferences.getString(keyCategory, defaultCategory)!!
}