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

    var _movie: Movie? = null
    val movie: Movie
        get() = _movie!!

    var isFavoriteMovie: Boolean = false
    var isWatchLaterMovie: Boolean = false

    var isConfigurationChanged: Boolean = false
    var isLowQualityPosterDownloaded: Boolean = false

    var fragmentThatLaunchedDetails: String? = null


    fun addToFavorite(movie: Movie) = interactor.addToFavorite(movie)

    fun removeFromFavorite(movie: Movie) = interactor.removeFromFavorite(movie)

    fun addToWatchLater(movie: Movie) = interactor.addToWatchLater(movie)

    fun removeFromWatchLater(movie: Movie) = interactor.removeFromWatchLater(movie)
}