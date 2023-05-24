package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.di.components.DaggerFavoritesMoviesComponent
import com.sandev.moviesearcher.di.components.FavoritesMoviesComponent
import com.sandev.moviesearcher.domain.interactors.FavoritesMoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FavoritesMoviesComponentViewModel : ViewModel() {

    @Inject
    lateinit var interactor: FavoritesMoviesListInteractor

    val favoritesMoviesComponent: FavoritesMoviesComponent

    init {
        favoritesMoviesComponent = DaggerFavoritesMoviesComponent.factory().create(App.instance.getAppComponent())
        favoritesMoviesComponent.inject(this)
    }
}