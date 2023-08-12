package com.sandev.cached_movies_feature.watch_later_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain_impl.local_database.di.components.DaggerWatchLaterMoviesDatabaseComponent
import com.sandev.cached_movies_feature.watch_later_movies.di.components.DaggerWatchLaterMoviesInteractorComponent


class WatchLaterMoviesComponentViewModel(application: Application) : AndroidViewModel(application) {

    val watchLaterMoviesComponent = DaggerWatchLaterMoviesInteractorComponent.builder()
        .database(DaggerWatchLaterMoviesDatabaseComponent.factory().create(application.applicationContext))
        .build()
}