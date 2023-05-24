package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.repositories.FavoritesMoviesRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.WatchLaterMoviesRepository
import com.sandev.moviesearcher.di.FavoriteFragmentScope
import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import com.sandev.moviesearcher.domain.Movie
import java.util.*
import javax.inject.Inject


sealed class MoviesListInteractor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

    val moviesListLiveData = MutableLiveData<List<Movie>>()


    fun addToList(movie: Movie) {
        repo.moviesList.add(movie)
        moviesListLiveData.postValue(repo.moviesList.toList())
    }

    fun removeFromList(movie: Movie) {
        repo.moviesList.remove(movie)
        moviesListLiveData.postValue(repo.moviesList.toList())
    }
}

@FavoriteFragmentScope
class FavoritesMoviesListInteractor @Inject constructor(repo: FavoritesMoviesRepository) : MoviesListInteractor(repo)

@WatchLaterFragmentScope
class WatchLaterMoviesListInteractor @Inject constructor(repo: WatchLaterMoviesRepository) : MoviesListInteractor(repo)