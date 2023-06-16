package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.repositories.PlayingMoviesListRepository
import com.sandev.moviesearcher.data.repositories.PopularMoviesListRepository
import com.sandev.moviesearcher.data.repositories.TopMoviesListRepository
import com.sandev.moviesearcher.data.repositories.UpcomingMoviesListRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class TmdbInteractorModule {

    @[Provides Singleton]
    fun provideTmdbInteractor(
        tmdbApi: TmdbApi,
        sharedPreferencesProvider: SharedPreferencesProvider,
        popularMoviesListRepository: PopularMoviesListRepository,
        topMoviesListRepository: TopMoviesListRepository,
        upcomingMoviesListRepository: UpcomingMoviesListRepository,
        playingMoviesListRepository: PlayingMoviesListRepository
    ): TmdbInteractor = TmdbInteractor(
        retrofitService = tmdbApi,
        sharedPreferences = sharedPreferencesProvider,
        popularMoviesListRepository,
        topMoviesListRepository,
        upcomingMoviesListRepository,
        playingMoviesListRepository
    )
}