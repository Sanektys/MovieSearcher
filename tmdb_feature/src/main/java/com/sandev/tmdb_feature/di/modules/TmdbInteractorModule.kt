package com.sandev.tmdb_feature.di.modules

import com.example.domain_api.the_movie_database.api.TmdbApi
import com.example.domain_impl.local_database.di.scopes.AllMoviesScope
import com.example.domain_impl.local_database.repositories.PlayingMoviesListRepository
import com.example.domain_impl.local_database.repositories.PopularMoviesListRepository
import com.example.domain_impl.local_database.repositories.TopMoviesListRepository
import com.example.domain_impl.local_database.repositories.UpcomingMoviesListRepository
import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor
import dagger.Module
import dagger.Provides


@Module
internal class TmdbInteractorModule {

    @[Provides AllMoviesScope]
    fun provideTmdbInteractor(
        tmdbApi: TmdbApi,
        popularMoviesListRepository: PopularMoviesListRepository,
        topMoviesListRepository: TopMoviesListRepository,
        upcomingMoviesListRepository: UpcomingMoviesListRepository,
        playingMoviesListRepository: PlayingMoviesListRepository
    ): TmdbInteractor = TmdbInteractor(
        retrofitService = tmdbApi,
        popularMoviesListRepository,
        topMoviesListRepository,
        upcomingMoviesListRepository,
        playingMoviesListRepository
    )
}