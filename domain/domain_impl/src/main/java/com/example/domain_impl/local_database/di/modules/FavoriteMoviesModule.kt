package com.example.domain_impl.local_database.di.modules

import android.content.Context
import androidx.room.Room
import com.example.domain_api.local_database.daos.FavoriteMovieDao
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.db_contracts.FavoriteDatabaseContract
import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_impl.local_database.databases.FavoriteMoviesDatabase
import com.example.domain_impl.local_database.repositories.MoviesListRepositoryForSavedListsImpl
import com.example.domain_impl.local_database.di.scopes.FavoriteMoviesScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [FavoriteMoviesRepositoryModule::class])
class FavoriteMoviesModule {

    @[Provides FavoriteMoviesScope]
    fun provideFavoriteMoviesDatabase(context: Context): FavoriteDatabaseContract = Room
        .databaseBuilder(context, FavoriteMoviesDatabase::class.java, FavoriteMoviesDatabase.DATABASE_NAME)
        .build()

    @[Provides FavoriteMoviesScope]
    fun provideFavoriteMovieDao(favoriteMoviesDatabase: FavoriteDatabaseContract): FavoriteMovieDao
            = favoriteMoviesDatabase.provideDao()

    @[Provides FavoriteMoviesScope]
    fun provideFavoriteMoviesListRepository(favoriteMovieDao: FavoriteMovieDao): MoviesListRepositoryForSavedListsImpl
            = MoviesListRepositoryForSavedListsImpl(favoriteMovieDao)
}

@Module
interface FavoriteMoviesRepositoryModule {

    @[Binds FavoriteMoviesScope]
    fun bindFavoriteMoviesListRepository(moviesListRepository: MoviesListRepositoryForSavedListsImpl): MoviesListRepository

    @[Binds FavoriteMoviesScope]
    fun bindFavoriteMoviesListRepositoryForSavedLists(moviesListRepository: MoviesListRepositoryForSavedListsImpl): MoviesListRepositoryForSavedLists

    @[Binds FavoriteMoviesScope]
    fun bindFavoriteMovieDao(favoriteMovieDao: FavoriteMovieDao): MovieDao
}

