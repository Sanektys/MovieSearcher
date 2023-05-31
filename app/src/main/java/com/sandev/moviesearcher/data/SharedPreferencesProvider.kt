package com.sandev.moviesearcher.data

import android.content.Context
import androidx.core.content.edit
import com.sandev.moviesearcher.R


class SharedPreferencesProvider(context: Context) {

    private val appContext = context.applicationContext

    private val sharedPreferences = appContext.getSharedPreferences(
        appContext.getString(R.string.shared_preferences_settings), Context.MODE_PRIVATE)

    init {
        if (sharedPreferences.getBoolean(appContext.getString(R.string.shared_preferences_settings_key_first_launch), false)) {
            sharedPreferences.edit { putBoolean(appContext.getString(R.string.shared_preferences_settings_key_first_launch), false) }
            sharedPreferences.edit { putString(appContext.getString(R.string.shared_preferences_settings_key_category), appContext.getString(R.string.shared_preferences_settings_default_category)) }
        }
    }


    fun setDefaultCategory(category: String) {
        sharedPreferences.edit {
            putString(appContext.getString(R.string.shared_preferences_settings_key_category), category)
        }
    }

    fun getDefaultCategory() = sharedPreferences.getString(
        appContext.getString(R.string.shared_preferences_settings_key_category),
        appContext.getString(R.string.shared_preferences_settings_default_category))!!
}