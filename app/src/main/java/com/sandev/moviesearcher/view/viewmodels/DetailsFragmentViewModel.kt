package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Movie


class DetailsFragmentViewModel : ViewModel() {

    val favoritesMoviesLiveData
        get() = interactor.favoritesMoviesLiveData
    val watchLaterMoviesLiveData
        get() = interactor.watchLaterMoviesLiveData

    private val interactor = App.instance.interactor


    fun addToFavorite(movie: Movie) = interactor.addToFavorite(movie)

    fun removeFromFavorite(movie: Movie) = interactor.removeFromFavorite(movie)

    fun addToWatchLater(movie: Movie) = interactor.addToWatchLater(movie)

    fun removeFromWatchLater(movie: Movie) = interactor.removeFromWatchLater(movie)
}