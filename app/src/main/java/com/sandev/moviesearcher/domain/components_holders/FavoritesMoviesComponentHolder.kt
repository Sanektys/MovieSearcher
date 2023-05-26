package com.sandev.moviesearcher.domain.components_holders

import com.sandev.moviesearcher.di.components.DaggerFavoritesMoviesComponent
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesMoviesComponentHolder {

    @Inject
    lateinit var interactor: MoviesListInteractor

    private val favoritesMoviesComponent = DaggerFavoritesMoviesComponent.factory().create()

    init {
        favoritesMoviesComponent.inject(this)
    }
}