package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import com.sandev.moviesearcher.di.FavoriteFragmentScope
import dagger.Binds
import dagger.Module


@Module
interface FavoriteMoviesListModule {
    @[Binds FavoriteFragmentScope]
    fun bindFavoriteMoviesList(moviesListRepository: MoviesListRepositoryImpl): MoviesListRepository
}

