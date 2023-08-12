package com.sandev.cached_movies_feature.favorite_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain_impl.local_database.di.components.DaggerFavoritesMoviesDatabaseComponent
import com.sandev.cached_movies_feature.favorite_movies.di.components.DaggerFavoritesMoviesInteractorComponent


class FavoriteMoviesComponentViewModel(application: Application) : AndroidViewModel(application) {

    val favoriteMoviesComponent = DaggerFavoritesMoviesInteractorComponent.builder()
        .database(DaggerFavoritesMoviesDatabaseComponent.factory().create(application.applicationContext))
        .build()
}