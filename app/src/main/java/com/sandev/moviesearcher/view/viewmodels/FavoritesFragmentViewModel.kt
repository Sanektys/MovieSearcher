package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter


class FavoritesFragmentViewModel(cachedMoviesInteractor: CachedMoviesInteractor)
    : SavedMoviesListViewModel(cachedMoviesInteractor) {

    override val recyclerAdapter: MoviesRecyclerAdapter
            = MoviesRecyclerAdapter(sharedPreferencesInteractor.isRatingDonutAnimationEnabled())

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode


    init {
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(recyclerAdapter.sharedPreferencesCallback)

        registerMovieInListStateChangeObservers()

        dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
    }


    class ViewModelFactory(private val cachedMoviesInteractor: CachedMoviesInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(FavoritesFragmentViewModel::class.java)) {
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