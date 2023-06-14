package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import javax.inject.Inject


class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder
    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    val favoritesMoviesLiveData
        get() = favoritesMoviesComponent.interactor.moviesListLiveData
    val watchLaterMoviesLiveData
        get() = watchLaterMoviesComponent.interactor.moviesListLiveData

    @Inject
    lateinit var interactor: TmdbInteractor

    var _movie: PopularMovie? = null
    val movie: PopularMovie
        get() = _movie!!

    var isFavoriteMovie: Boolean = false
    var isWatchLaterMovie: Boolean = false

    var isConfigurationChanged: Boolean = false
    var isLowQualityPosterDownloaded: Boolean = false

    var fragmentThatLaunchedDetails: String? = null

    init {
        App.instance.getAppComponent().inject(this)
    }


    fun addToFavorite(movie: PopularMovie) = favoritesMoviesComponent.interactor.addToList(movie)

    fun removeFromFavorite(movie: PopularMovie) = favoritesMoviesComponent.interactor.removeFromList(movie)

    fun addToWatchLater(movie: PopularMovie) = watchLaterMoviesComponent.interactor.addToList(movie)

    fun removeFromWatchLater(movie: PopularMovie) = watchLaterMoviesComponent.interactor.removeFromList(movie)
}