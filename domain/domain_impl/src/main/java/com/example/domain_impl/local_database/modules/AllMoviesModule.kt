package com.example.domain_impl.local_database.modules

import android.content.Context
import androidx.room.Room
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.daos.PlayingMovieDao
import com.example.domain_api.local_database.daos.PopularMovieDao
import com.example.domain_api.local_database.daos.TopMovieDao
import com.example.domain_api.local_database.daos.UpcomingMovieDao
import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_impl.local_database.databases.AllMoviesDatabase
import com.example.domain_impl.local_database.repositories.MoviesListRepositoryImpl
import com.example.domain_impl.local_database.repositories.PlayingMoviesListRepository
import com.example.domain_impl.local_database.repositories.PopularMoviesListRepository
import com.example.domain_impl.local_database.repositories.TopMoviesListRepository
import com.example.domain_impl.local_database.repositories.UpcomingMoviesListRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(includes = [AllMoviesRepositoryModule::class])
class AllMoviesModule {

    @[Provides Singleton]
    fun provideAllMoviesDatabase(context: Context) : AllMoviesDatabase = Room
        .databaseBuilder(context, AllMoviesDatabase::class.java, AllMoviesDatabase.DATABASE_NAME)
        .build()


    @[Provides Singleton]
    fun providePopularMovieDao(allMoviesDatabase: AllMoviesDatabase): PopularMovieDao
            = allMoviesDatabase.providePopularMoviesDao()

    @[Provides Singleton]
    fun provideTopMovieDao(allMoviesDatabase: AllMoviesDatabase): TopMovieDao
            = allMoviesDatabase.provideTopMoviesDao()

    @[Provides Singleton]
    fun provideUpcomingMovieDao(allMoviesDatabase: AllMoviesDatabase): UpcomingMovieDao
            = allMoviesDatabase.provideUpcomingMoviesDao()

    @[Provides Singleton]
    fun providePlayingMovieDao(allMoviesDatabase: AllMoviesDatabase): PlayingMovieDao
            = allMoviesDatabase.providePlayingMoviesDao()


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
interface AllMoviesRepositoryModule {

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