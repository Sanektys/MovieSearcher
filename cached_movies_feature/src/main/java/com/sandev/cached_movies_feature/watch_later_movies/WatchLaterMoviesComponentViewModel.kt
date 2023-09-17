package com.sandev.cached_movies_feature.watch_later_movies

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.provideWatchLaterMoviesDatabase
import com.sandev.cached_movies_feature.watch_later_movies.di.components.DaggerWatchLaterMoviesInteractorComponent
import com.sandev.cached_movies_feature.watch_later_movies.domain.WatchLaterMoviesInteractor
import javax.inject.Inject


class WatchLaterMoviesComponentViewModel(context: Context) : ViewModel() {

    private val watchLaterMoviesComponent = DaggerWatchLaterMoviesInteractorComponent.builder()
        .database(provideWatchLaterMoviesDatabase(context))
        .build()

    @Inject
    lateinit var interactor: WatchLaterMoviesInteractor


    init {
        watchLaterMoviesComponent.inject(this)
    }


    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(WatchLaterMoviesComponentViewModel::class.java)) {
                return WatchLaterMoviesComponentViewModel(context) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}