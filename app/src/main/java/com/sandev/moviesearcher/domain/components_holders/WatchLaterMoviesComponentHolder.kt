package com.sandev.moviesearcher.domain.components_holders

import android.content.Context
import com.sandev.moviesearcher.di.components.DaggerWatchLaterMoviesComponent
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WatchLaterMoviesComponentHolder(context: Context) : SavedMoviesComponentHolder {

    @Inject
    override lateinit var interactor: MoviesListInteractor

    private val watchLaterMoviesComponent = DaggerWatchLaterMoviesComponent.factory().create(context)

    init {
        watchLaterMoviesComponent.inject(this)
    }
}