package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import javax.inject.Inject


class FavoritesFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder

    override val moviesListLiveData: LiveData<List<Movie>>

    override var lastSearch: String?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotFavorite: Boolean = false
    var lastClickedMovie: Movie? = null

    companion object {
        private var lastSearch: String? = null
    }

    init {
        App.instance.getAppComponent().inject(this)

        moviesListLiveData = favoritesMoviesComponent.interactor.getAllFromList()
    }


    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, getAllMovies())
    }

    fun addToFavorite(movie: Movie) = favoritesMoviesComponent.interactor.addToList(movie)

    fun removeFromFavorite(movie: Movie) = favoritesMoviesComponent.interactor.removeFromList(movie)
}