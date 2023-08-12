package com.sandev.cached_movies_feature.favorite_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain.provideFavoritesMoviesDatabase
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.cached_movies_feature.favorite_movies.di.components.DaggerFavoritesMoviesInteractorComponent
import javax.inject.Inject


class FavoriteMoviesComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteMoviesComponent = DaggerFavoritesMoviesInteractorComponent.builder()
        .database(provideFavoritesMoviesDatabase(application.applicationContext))
        .build()

    @Inject
    lateinit var interactor: CachedMoviesInteractor


    init {
        favoriteMoviesComponent.inject(this)
    }
}