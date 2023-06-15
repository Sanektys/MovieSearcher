package com.sandev.moviesearcher.di.modules

import android.content.Context
import androidx.room.Room
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.WatchLaterMovieDao
import com.sandev.moviesearcher.data.db.databases.WatchLaterMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [WatchLaterMoviesListRepositoryModule::class])
class WatchLaterMoviesListModule {

    @[Provides WatchLaterFragmentScope]
    fun provideWatchLaterMoviesDatabase(context: Context): WatchLaterMoviesDatabase = Room
        .databaseBuilder(context, WatchLaterMoviesDatabase::class.java, WatchLaterMoviesDatabase.DATABASE_NAME)
        .build()

    @[Provides WatchLaterFragmentScope]
    fun provideWatchLaterMovieDao(): WatchLaterMovieDao = WatchLaterMoviesDatabase.watchLaterMovieDao()

    @[Provides WatchLaterFragmentScope]
    fun provideWatchLaterMoviesRepository(watchLaterMovieDao: WatchLaterMovieDao) : MoviesListRepositoryImplWithList
            = MoviesListRepositoryImplWithList(watchLaterMovieDao)
}

@Module
interface WatchLaterMoviesListRepositoryModule {

    @[Binds WatchLaterFragmentScope]
    fun bindWatchLaterMoviesList(moviesListRepository: MoviesListRepositoryImplWithList): MoviesListRepository

    @[Binds WatchLaterFragmentScope]
    fun bindWatchLaterMovieDao(watchLaterMovieDao: WatchLaterMovieDao): MovieDao
}