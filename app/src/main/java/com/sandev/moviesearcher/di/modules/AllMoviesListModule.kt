package com.sandev.moviesearcher.di.modules

import android.content.Context
import androidx.room.Room
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.PlayingMovieDao
import com.sandev.moviesearcher.data.db.dao.PopularMovieDao
import com.sandev.moviesearcher.data.db.dao.TopMovieDao
import com.sandev.moviesearcher.data.db.dao.UpcomingMovieDao
import com.sandev.moviesearcher.data.db.databases.AllMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import com.sandev.moviesearcher.data.repositories.PlayingMoviesListRepository
import com.sandev.moviesearcher.data.repositories.PopularMoviesListRepository
import com.sandev.moviesearcher.data.repositories.TopMoviesListRepository
import com.sandev.moviesearcher.data.repositories.UpcomingMoviesListRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(includes = [AllMoviesListRepositoryModule::class])
class AllMoviesListModule {

    @[Provides Singleton]
    fun provideAllMoviesDatabase(context: Context) : AllMoviesDatabase = Room
        .databaseBuilder(context, AllMoviesDatabase::class.java, AllMoviesDatabase.DATABASE_NAME)
        .build()

    @[Provides Singleton]
    fun providePopularMovieDao(): PopularMovieDao = AllMoviesDatabase.popularMovieDao()

    @[Provides Singleton]
    fun provideTopMovieDao(): TopMovieDao = AllMoviesDatabase.topMovieDao()

    @[Provides Singleton]
    fun provideUpcomingMovieDao(): UpcomingMovieDao = AllMoviesDatabase.upcomingMovieDao()

    @[Provides Singleton]
    fun providePlayingMovieDao(): PlayingMovieDao = AllMoviesDatabase.playingMovieDao()

    @[Provides Singleton]
    fun providePopularMoviesListRepository(popularMovieDao: PopularMovieDao): PopularMoviesListRepository
            = PopularMoviesListRepository(popularMovieDao)

    @[Provides Singleton]
    fun provideTopMoviesListRepository(topMovieDao: TopMovieDao): TopMoviesListRepository
            = TopMoviesListRepository(topMovieDao)

    @[Provides Singleton]
    fun provideUpcomingMoviesListRepository(upcomingMovieDao: UpcomingMovieDao): UpcomingMoviesListRepository
            = UpcomingMoviesListRepository(upcomingMovieDao)

    @[Provides Singleton]
    fun providePlayingMoviesListRepository(playingMovieDao: PlayingMovieDao): PlayingMoviesListRepository
            = PlayingMoviesListRepository(playingMovieDao)
}

@Module
interface AllMoviesListRepositoryModule {

    @[Binds Singleton]
    fun bindMoviesListRepository(allMoviesListRepository: MoviesListRepositoryImpl): MoviesListRepository

    @[Binds Singleton]
    fun bindPopularMovieDao(popularMovieDao: PopularMovieDao): MovieDao

    @[Binds Singleton]
    fun bindTopMovieDao(topMovieDao: TopMovieDao): MovieDao

    @[Binds Singleton]
    fun bindUpcomingMovieDao(upcomingMovieDao: UpcomingMovieDao): MovieDao

    @[Binds Singleton]
    fun bindPlayingMovieDao(playingMovieDao: PlayingMovieDao): MovieDao
}