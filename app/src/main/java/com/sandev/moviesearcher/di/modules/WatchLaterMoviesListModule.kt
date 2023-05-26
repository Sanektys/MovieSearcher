package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import dagger.Binds
import dagger.Module


@Module
interface WatchLaterMoviesListModule {
    @[Binds WatchLaterFragmentScope]
    fun bindFavoriteMoviesList(moviesListRepository: MoviesListRepositoryImpl): MoviesListRepository
}