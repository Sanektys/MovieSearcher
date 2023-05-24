package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.di.components.DaggerWatchLaterMoviesComponent
import com.sandev.moviesearcher.di.components.WatchLaterMoviesComponent
import com.sandev.moviesearcher.domain.interactors.WatchLaterMoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchLaterMoviesComponentViewModel : ViewModel() {

    @Inject
    lateinit var interactor: WatchLaterMoviesListInteractor

    val watchLaterMoviesComponent: WatchLaterMoviesComponent

    init {
        watchLaterMoviesComponent = DaggerWatchLaterMoviesComponent.factory().create(App.instance.getAppComponent())
        watchLaterMoviesComponent.inject(this)
    }
}