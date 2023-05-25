package com.sandev.moviesearcher.domain.components_holders

import com.sandev.moviesearcher.di.components.DaggerWatchLaterMoviesComponent
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchLaterMoviesComponentHolder {

    @Inject
    lateinit var interactor: MoviesListInteractor

    private val watchLaterMoviesComponent = DaggerWatchLaterMoviesComponent.factory().create()

    init {
        watchLaterMoviesComponent.inject(this)
    }
}