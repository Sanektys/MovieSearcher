package com.sandev.tmdb_feature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain.provideAllMoviesDatabase
import com.example.domain.provideRetrofit
import com.sandev.tmdb_feature.di.components.DaggerTmdbInteractorComponent
import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor


class TmdbComponentViewModel(application: Application) : AndroidViewModel(application) {

    private val tmdbComponent = DaggerTmdbInteractorComponent.builder()
        .database(provideAllMoviesDatabase(application.applicationContext))
        .retrofit(provideRetrofit())
        .build()

    lateinit var interactor: TmdbInteractor


    init {
        tmdbComponent.inject(this)
    }
}