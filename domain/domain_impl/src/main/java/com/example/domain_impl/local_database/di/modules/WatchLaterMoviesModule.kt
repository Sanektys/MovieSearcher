package com.example.domain_impl.local_database.di.modules

import android.content.Context
import androidx.room.Room
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.daos.WatchLaterMovieDao
import com.example.domain_api.local_database.db_contracts.WatchLaterDatabaseContract
import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_impl.local_database.databases.WatchLaterMoviesDatabase
import com.example.domain_impl.local_database.repositories.MoviesListRepositoryForSavedListsImpl
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [WatchLaterMoviesRepositoryModule::class])
class WatchLaterMoviesModule {

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesDatabase(context: Context): WatchLaterDatabaseContract = Room
        .databaseBuilder(context, WatchLaterMoviesDatabase::class.java, WatchLaterMoviesDatabase.DATABASE_NAME)
        .build()

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMovieDao(watchLaterMoviesDatabase: WatchLaterDatabaseContract): WatchLaterMovieDao
            = watchLaterMoviesDatabase.provideDao()

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesRepository(watchLaterMovieDao: WatchLaterMovieDao) : MoviesListRepositoryForSavedListsImpl
            = MoviesListRepositoryForSavedListsImpl(watchLaterMovieDao)
}

@Module
interface WatchLaterMoviesRepositoryModule {

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMoviesListRepository(moviesListRepository: MoviesListRepositoryForSavedListsImpl): MoviesListRepository

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMoviesListRepositoryForSavedLists(moviesListRepository: MoviesListRepositoryForSavedListsImpl): MoviesListRepositoryForSavedLists

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMovieDao(watchLaterMovieDao: WatchLaterMovieDao): MovieDao
}