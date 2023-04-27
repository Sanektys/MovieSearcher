package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie


class FavoritesFragmentViewModel : MoviesListFragmentViewModel() {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    override val interactor: Interactor = App.instance.interactor

    override var lastSearch: CharSequence?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotFavorite: Boolean = false
    var lastClickedMovie: Movie? = null

    companion object {
        private var lastSearch: CharSequence? = null

        private val favoritesMovies = mutableListOf<Movie>()
    }

    init {
        moviesListLiveData.postValue(favoritesMovies.toList())
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun addToFavorite(movie: Movie) {
        favoritesMovies.add(movie)
        moviesListLiveData.postValue(favoritesMovies.toList())
    }

    fun removeFromFavorite(movie: Movie) {
        favoritesMovies.remove(movie)
        moviesListLiveData.postValue(favoritesMovies.toList())
    }

    fun removeFromFavoriteAt(position: Int) {
        favoritesMovies.removeAt(position)
        moviesListLiveData.postValue(favoritesMovies.toList())
    }
}