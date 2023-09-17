package com.sandev.cached_movies_feature.watch_later_movies.di.modules

import com.example.domain_api.local_database.repository.WatchLaterListRepository
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import com.sandev.cached_movies_feature.watch_later_movies.domain.WatchLaterMoviesInteractor
import dagger.Module
import dagger.Provides


@Module
internal class WatchLaterMoviesInteractorModule {

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesDatabaseInteractor(moviesListRepository: WatchLaterListRepository): WatchLaterMoviesInteractor
        = WatchLaterMoviesInteractor(moviesListRepository)
}