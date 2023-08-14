package com.sandev.cached_movies_feature.favorite_movies.di

import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor


internal interface FavoritesMoviesInteractorProvider {
    fun provideInteractor(): CachedMoviesInteractor
}