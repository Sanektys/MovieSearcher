package com.sandev.cached_movies_feature.watch_later_movies.di

import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor


interface WatchLaterMoviesInteractorProvider {
    fun provideInteractor(): CachedMoviesInteractor
}