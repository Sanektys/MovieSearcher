package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.rv_adapters.WatchLaterRecyclerAdapter


class WatchLaterFragmentViewModel(cachedMoviesInteractor: CachedMoviesInteractor)
    : SavedMoviesListViewModel(cachedMoviesInteractor) {

    override val recyclerAdapter: MoviesRecyclerAdapter
            = WatchLaterRecyclerAdapter(sharedPreferencesInteractor.isRatingDonutAnimationEnabled())

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode

    var isLaunchedFromLeft: Boolean = true


    init {
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(recyclerAdapter.sharedPreferencesCallback)

        registerMovieInListStateChangeObservers()
    }


    fun blockOnClickCallbacksOnMovieCardElements() {
        super.blockCallbackOnPosterClick()
        (recyclerAdapter as WatchLaterRecyclerAdapter).setOnScheduleNotificationButtonClick(null)
    }

    fun unblockOnClickCallbacksOnMovieCardElements(onPosterClick: MoviesRecyclerAdapter.OnPosterClickListener,
                                                   onScheduleButtonClick: WatchLaterRecyclerAdapter.ScheduleNotificationButtonClick) {
        super.unblockCallbackOnPosterClick(onPosterClick)
        (recyclerAdapter as WatchLaterRecyclerAdapter).setOnScheduleNotificationButtonClick(onScheduleButtonClick)
    }


    class ViewModelFactory(private val cachedMoviesInteractor: CachedMoviesInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(WatchLaterFragmentViewModel::class.java)) {
                return WatchLaterFragmentViewModel(cachedMoviesInteractor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false
    }
}
