package com.sandev.moviesearcher.view.viewmodels

import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import javax.inject.Inject


class WatchLaterFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    override val moviesListLiveData
        get() = watchLaterMoviesComponent.interactor.moviesListLiveData

    override var lastSearch: String?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotWatchLater: Boolean = false
    var lastClickedMovie: PopularMovie? = null

    companion object {
        private var lastSearch: String? = null
    }

    init {
        App.instance.getAppComponent().inject(this)
    }


    override fun searchInDatabase(query: CharSequence): List<PopularMovie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun addToWatchLater(movie: PopularMovie) = watchLaterMoviesComponent.interactor.addToList(movie)

    fun removeFromWatchLater(movie: PopularMovie) = watchLaterMoviesComponent.interactor.removeFromList(movie)
}