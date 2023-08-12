package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor


class FavoritesFragmentViewModel(cachedMoviesInteractor: CachedMoviesInteractor)
    : SavedMoviesListViewModel(cachedMoviesInteractor) {

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode


    class ViewModelFactory(private val cachedMoviesInteractor: CachedMoviesInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                return FavoritesFragmentViewModel(cachedMoviesInteractor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false
    }
}