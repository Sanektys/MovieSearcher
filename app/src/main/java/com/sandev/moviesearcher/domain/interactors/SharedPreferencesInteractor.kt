package com.sandev.moviesearcher.domain.interactors

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class SharedPreferencesInteractor(private val sharedPreferences: SharedPreferencesProvider) {

    suspend fun setDefaultMoviesCategoryInMainList(category: String) = withContext(Dispatchers.IO) {
        sharedPreferences.setDefaultCategory(category)
    }

    suspend fun getDefaultMoviesCategoryInMainList() = withContext(Dispatchers.IO) {
        sharedPreferences.getDefaultCategory()
    }

    suspend fun setShowingSplashScreen(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.setShowingSplashScreen(isEnabled)
    }

    suspend fun getSplashScreenEnabled() = withContext(Dispatchers.IO) {
        sharedPreferences.isSplashScreenEnabled()
    }

    fun isSplashScreenEnabled() = sharedPreferences.isSplashScreenEnabled()

    fun addSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.registerSharedPreferencesChangeListener(listener)

    fun removeSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.unregisterSharedPreferencesChangeListener(listener)
}