package com.sandev.cached_movies_feature.watch_later_movies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain.provideWatchLaterMoviesDatabase
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.cached_movies_feature.watch_later_movies.di.components.DaggerWatchLaterMoviesInteractorComponent
import javax.inject.Inject


class WatchLaterMoviesComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val watchLaterMoviesComponent = DaggerWatchLaterMoviesInteractorComponent.builder()
        .database(provideWatchLaterMoviesDatabase(application.applicationContext))
        .build()

    @Inject
    lateinit var interactor: CachedMoviesInteractor


    init {
        watchLaterMoviesComponent.inject(this)
    }
}