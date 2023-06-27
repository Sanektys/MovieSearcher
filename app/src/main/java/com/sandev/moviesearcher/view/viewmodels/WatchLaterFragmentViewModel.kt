package com.sandev.moviesearcher.view.viewmodels

import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import javax.inject.Inject


class WatchLaterFragmentViewModel : SavedMoviesListViewModel() {

    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode

    var isLaunchedFromLeft: Boolean = true


    init {
        App.instance.getAppComponent().inject(this)

        savedMoviesComponent = watchLaterMoviesComponent

        watchLaterMoviesComponent.interactor.deletedMovieLiveData.observeForever(movieDeletedObserver)
        watchLaterMoviesComponent.interactor.movieAddedNotifyLiveData.observeForever(movieAddedObserver)

        getMoviesFromDB(
            offset = moviesPaginationOffset,
            moviesCount = moviesPerPage
        )
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false
    }
}
