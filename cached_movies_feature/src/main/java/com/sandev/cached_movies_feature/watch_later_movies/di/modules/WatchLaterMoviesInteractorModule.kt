package com.sandev.cached_movies_feature.watch_later_movies.di.modules

import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import dagger.Module
import dagger.Provides


@Module
class WatchLaterMoviesInteractorModule {

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesDatabaseInteractor(moviesListRepository: MoviesListRepositoryForSavedLists) =
        CachedMoviesInteractor(moviesListRepository)
}