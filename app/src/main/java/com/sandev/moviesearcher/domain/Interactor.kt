package com.sandev.moviesearcher.domain

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.MainRepository


class Interactor(private val repo: MainRepository) {

    val favoritesMoviesLiveData = MutableLiveData<List<Movie>>()
    val watchLaterMoviesLiveData = MutableLiveData<List<Movie>>()

    init {
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }

    fun getMoviesDB(): List<Movie> = repo.moviesDatabase

    fun addToFavorite(movie: Movie) {
        repo.favoritesMovies.add(movie)
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
    }

    fun removeFromFavorite(movie: Movie) {
        repo.favoritesMovies.remove(movie)
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
    }

    fun addToWatchLater(movie: Movie) {
        repo.watchLaterMovies.add(movie)
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }

    fun removeFromWatchLater(movie: Movie) {
        repo.watchLaterMovies.remove(movie)
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }
}