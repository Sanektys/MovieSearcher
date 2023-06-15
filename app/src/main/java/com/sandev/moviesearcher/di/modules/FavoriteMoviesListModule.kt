package com.sandev.moviesearcher.di.modules

import android.content.Context
import androidx.room.Room
import com.sandev.moviesearcher.data.db.dao.FavoriteMovieDao
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.databases.FavoriteMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import com.sandev.moviesearcher.di.FavoriteFragmentScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [FavoriteMoviesListRepositoryModule::class])
class FavoriteMoviesListModule {

    @[Provides FavoriteFragmentScope]
    fun provideFavoriteMoviesDatabase(context: Context): FavoriteMoviesDatabase = Room
        .databaseBuilder(context, FavoriteMoviesDatabase::class.java, FavoriteMoviesDatabase.DATABASE_NAME)
        .build()

    @[Provides FavoriteFragmentScope]
    fun provideFavoriteMovieDao(favoriteMoviesDatabase: FavoriteMoviesDatabase): FavoriteMovieDao
            = favoriteMoviesDatabase.favoriteMovieDao()

    @[Provides FavoriteFragmentScope]
    fun provideFavoriteMoviesListRepository(favoriteMovieDao: FavoriteMovieDao): MoviesListRepositoryImplWithList
            = MoviesListRepositoryImplWithList(favoriteMovieDao)
}

@Module
interface FavoriteMoviesListRepositoryModule {

    @[Binds FavoriteFragmentScope]
    fun bindFavoriteMoviesList(moviesListRepository: MoviesListRepositoryImplWithList): MoviesListRepository

    @[Binds FavoriteFragmentScope]
    fun bindFavoriteMovieDao(favoriteMovieDao: FavoriteMovieDao): MovieDao
}

