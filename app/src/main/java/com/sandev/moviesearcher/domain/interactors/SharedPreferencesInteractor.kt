package com.sandev.moviesearcher.domain.interactors

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.sandev.moviesearcher.data.SharedPreferencesProvider


class SharedPreferencesInteractor(private val sharedPreferences: SharedPreferencesProvider) {

    fun setDefaultMoviesCategoryInMainList(category: String) = sharedPreferences.setDefaultCategory(category)

    fun getDefaultMoviesCategoryInMainList() = sharedPreferences.getDefaultCategory()

    fun addSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.registerSharedPreferencesChangeListener(listener)

    fun removeSharedPreferencesChangeListener(listener: OnSharedPreferenceChangeListener) =
        sharedPreferences.unregisterSharedPreferencesChangeListener(listener)
}