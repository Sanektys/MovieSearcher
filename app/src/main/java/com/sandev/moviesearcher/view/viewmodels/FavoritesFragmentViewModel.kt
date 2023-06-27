package com.sandev.moviesearcher.view.viewmodels

import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import javax.inject.Inject


class FavoritesFragmentViewModel : SavedMoviesListViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode


    init {
        App.instance.getAppComponent().inject(this)

        savedMoviesComponent = favoritesMoviesComponent

        favoritesMoviesComponent.interactor.deletedMovieLiveData.observeForever(movieDeletedObserver)
        favoritesMoviesComponent.interactor.movieAddedNotifyLiveData.observeForever(movieAddedObserver)

        dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false
    }
}