package com.sandev.moviesearcher.view.viewmodels

import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class FavoritesFragmentViewModel @Inject constructor(interactor: Interactor) : MoviesListFragmentViewModel(interactor) {

    override val moviesListLiveData
        get() = interactor.favoritesMoviesLiveData

    override var lastSearch: String?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotFavorite: Boolean = false
    var lastClickedMovie: Movie? = null

    companion object {
        private var lastSearch: String? = null
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun addToFavorite(movie: Movie) = interactor.addToFavorite((movie))

    fun removeFromFavorite(movie: Movie) = interactor.removeFromFavorite(movie)
}