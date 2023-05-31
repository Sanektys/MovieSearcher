package com.sandev.moviesearcher.domain.interactors

import com.sandev.moviesearcher.data.SharedPreferencesProvider


class SharedPreferencesInteractor(private val sharedPreferences: SharedPreferencesProvider) {

    fun setDefaultMoviesCategoryInMainList(category: String) = sharedPreferences.setDefaultCategory(category)

    fun getDefaultMoviesCategoryInMainList() = sharedPreferences.getDefaultCategory()
}