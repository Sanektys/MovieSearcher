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

    fun setAppTheme(nightMode: String) = sharedPreferences.setAppTheme(nightMode)

    fun getAppTheme() = sharedPreferences.getAppTheme()

    suspend fun setShowingSplashScreen(isEnabled: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.setShowingSplashScreen(isEnabled)
    }

    suspend fun getSplashScreenEnabled() = withContext(Dispatchers.IO) {
        sharedPreferences.isSplashScreenEnabled() && sharedPreferences.isSplashScreenSwitchButtonEnabled()
    }

    fun isSplashScreenEnabled()
            = sharedPreferences.isSplashScreenEnabled() && sharedPreferences.isSplashScreenSwitchButtonEnabled()

    fun setRatingDonutAnimationState(isEnabled: Boolean)
            = sharedPreferences.setRatingDonutAnimationState(isEnabled)

    fun isRatingDonutAnimationEnabled()
            = sharedPreferences.isRatingDonutAnimationEnabled() && sharedPreferences.isRatingDonutSwitchButtonEnabled()

    fun setSplashScreenSwitchButtonState(isEnabled: Boolean)
            = sharedPreferences.setSplashScreenSwitchButtonState(isEnabled)

    fun isSplashScreenSwitchButtonEnabled() = sharedPreferences.isSplashScreenSwitchButtonEnabled()

    fun setRatingDonutSwitchButtonState(isEnabled: Boolean)
            = sharedPreferences.setRatingDonutSwitchButtonState(isEnabled)

    fun isRatingDonutSwitchButtonEnabled() = sharedPreferences.isRatingDonutSwitchButtonEnabled()

    fun addSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.registerSharedPreferencesChangeListener(listener)

    fun removeSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.unregisterSharedPreferencesChangeListener(listener)
}