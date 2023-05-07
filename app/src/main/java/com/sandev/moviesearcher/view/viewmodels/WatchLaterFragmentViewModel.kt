package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie


class WatchLaterFragmentViewModel : MoviesListFragmentViewModel() {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    override val interactor: Interactor = App.instance.interactor

    override var lastSearch: CharSequence?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotWatchLater: Boolean = false
    var lastClickedMovie: Movie? = null

    companion object {
        private var lastSearch: CharSequence? = null

        private val watchLaterMovies = mutableListOf<Movie>()
    }

    init {
        moviesListLiveData.postValue(watchLaterMovies.toList())
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun addToWatchLater(movie: Movie) {
        watchLaterMovies.add(movie)
        moviesListLiveData.postValue(watchLaterMovies.toList())
    }

    fun removeFromWatchLater(movie: Movie) {
        watchLaterMovies.remove(movie)
        moviesListLiveData.postValue(watchLaterMovies.toList())
    }

    fun removeFromWatchLaterAt(position: Int) {
        watchLaterMovies.removeAt(position)
        moviesListLiveData.postValue(watchLaterMovies.toList())
    }
}