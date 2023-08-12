package com.sandev.cached_movies_feature.favorite_movies.di.modules

import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_impl.local_database.di.scopes.FavoriteMoviesScope
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import dagger.Module
import dagger.Provides


@Module
internal class FavoritesMoviesInteractorModule {

    @[Provides FavoriteMoviesScope]
    fun provideFavoriteMoviesDatabaseInteractor(moviesListRepository: MoviesListRepositoryForSavedLists) =
        CachedMoviesInteractor(moviesListRepository)
}