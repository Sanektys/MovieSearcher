package com.example.domain_impl.local_database.di.modules

import android.content.Context
import androidx.room.Room
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.daos.WatchLaterMovieDao
import com.example.domain_api.local_database.db_contracts.WatchLaterDatabaseContract
import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_api.local_database.repository.WatchLaterListRepository
import com.example.domain_impl.local_database.databases.WatchLaterMoviesDatabase
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import com.example.domain_impl.local_database.migrations.WatchLaterMoviesDatabaseMigrations
import com.example.domain_impl.local_database.repositories.WatchLaterListRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [WatchLaterMoviesRepositoryModule::class])
class WatchLaterMoviesModule {

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesDatabase(context: Context): WatchLaterDatabaseContract = Room
        .databaseBuilder(context, WatchLaterMoviesDatabase::class.java, WatchLaterMoviesDatabase.DATABASE_NAME)
        .addMigrations(WatchLaterMoviesDatabaseMigrations.migration_1_2)
        .build()

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMovieDao(watchLaterMoviesDatabase: WatchLaterDatabaseContract): WatchLaterMovieDao
            = watchLaterMoviesDatabase.provideDao()

    @[Provides WatchLaterMoviesScope]
    fun provideWatchLaterMoviesRepository(watchLaterMovieDao: WatchLaterMovieDao) : WatchLaterListRepositoryImpl
            = WatchLaterListRepositoryImpl(watchLaterMovieDao)
}

@Module
interface WatchLaterMoviesRepositoryModule {

    @[Binds WatchLaterMoviesScope]
    fun bindBaseWatchLaterMoviesListRepository(moviesListRepository: WatchLaterListRepositoryImpl): MoviesListRepository

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMoviesListRepositoryForSavedLists(moviesListRepository: WatchLaterListRepositoryImpl): MoviesListRepositoryForSavedLists

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMoviesListRepository(moviesListRepository: WatchLaterListRepositoryImpl): WatchLaterListRepository

    @[Binds WatchLaterMoviesScope]
    fun bindWatchLaterMovieDao(watchLaterMovieDao: WatchLaterMovieDao): MovieDao
}