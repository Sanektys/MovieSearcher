package com.sandev.moviesearcher.domain.components_holders

import android.content.Context
import com.sandev.moviesearcher.di.components.DaggerFavoritesMoviesComponent
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesMoviesComponentHolder(context: Context) {

    @Inject
    lateinit var interactor: MoviesListInteractor

    private val favoritesMoviesComponent = DaggerFavoritesMoviesComponent.factory().create(context)

    init {
        favoritesMoviesComponent.inject(this)
    }
}