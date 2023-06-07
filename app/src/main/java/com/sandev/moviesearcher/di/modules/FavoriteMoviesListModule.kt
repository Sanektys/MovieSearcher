package com.sandev.moviesearcher.di.modules

import android.content.Context
import com.sandev.moviesearcher.data.db.FavoriteMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import com.sandev.moviesearcher.di.FavoriteFragmentScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [FavoriteMoviesListRepositoryModule::class])
class FavoriteMoviesListModule {

    @[Provides FavoriteFragmentScope]
    fun provideFavoriteMoviesDatabase(context: Context): FavoriteMoviesDatabase = FavoriteMoviesDatabase(context)

    @[Provides FavoriteFragmentScope]
    fun provideFavoriteMoviesListRepository(moviesDatabase: FavoriteMoviesDatabase): MoviesListRepositoryImpl
            = MoviesListRepositoryImpl(moviesDatabase)
}

@Module
interface FavoriteMoviesListRepositoryModule {

    @[Binds FavoriteFragmentScope]
    fun bindFavoriteMoviesList(moviesListRepository: MoviesListRepositoryImpl): MoviesListRepository
}

