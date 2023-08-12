package com.sandev.tmdb_feature

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.domain_impl.local_database.di.components.DaggerAllMoviesDatabaseComponent
import com.example.domain_impl.the_movie_database.di.components.DaggerTmdbRetrofitComponent
import com.sandev.tmdb_feature.di.components.DaggerTmdbInteractorComponent


class TmdbComponentViewModel(application: Application) : AndroidViewModel(application) {

    val tmdbComponent = DaggerTmdbInteractorComponent.builder()
        .database(DaggerAllMoviesDatabaseComponent.factory().create(application.applicationContext))
        .retrofit(DaggerTmdbRetrofitComponent.create())
        .build()
}