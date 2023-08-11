package com.example.domain_impl.local_database.di.modules

import android.content.Context
import androidx.room.Room
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.daos.PlayingMovieDao
import com.example.domain_api.local_database.daos.PopularMovieDao
import com.example.domain_api.local_database.daos.TopMovieDao
import com.example.domain_api.local_database.daos.UpcomingMovieDao
import com.example.domain_api.local_database.db_contracts.AllMoviesDatabaseContract
import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_impl.local_database.databases.AllMoviesDatabase
import com.example.domain_impl.local_database.repositories.MoviesListRepositoryImpl
import com.example.domain_impl.local_database.repositories.PlayingMoviesListRepository
import com.example.domain_impl.local_database.repositories.PopularMoviesListRepository
import com.example.domain_impl.local_database.repositories.TopMoviesListRepository
import com.example.domain_impl.local_database.repositories.UpcomingMoviesListRepository
import com.example.domain_impl.local_database.di.scopes.AllMoviesScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [AllMoviesRepositoryModule::class])
class AllMoviesModule {

    @[Provides AllMoviesScope]
    fun provideAllMoviesDatabase(context: Context) : AllMoviesDatabaseContract = Room
        .databaseBuilder(context, AllMoviesDatabase::class.java, AllMoviesDatabase.DATABASE_NAME)
        .build()


    @[Provides AllMoviesScope]
    fun providePopularMovieDao(allMoviesDatabase: AllMoviesDatabaseContract): PopularMovieDao
            = allMoviesDatabase.providePopularMoviesDao()

    @[Provides AllMoviesScope]
    fun provideTopMovieDao(allMoviesDatabase: AllMoviesDatabaseContract): TopMovieDao
            = allMoviesDatabase.provideTopMoviesDao()

    @[Provides AllMoviesScope]
    fun provideUpcomingMovieDao(allMoviesDatabase: AllMoviesDatabaseContract): UpcomingMovieDao
            = allMoviesDatabase.provideUpcomingMoviesDao()

    @[Provides AllMoviesScope]
    fun providePlayingMovieDao(allMoviesDatabase: AllMoviesDatabaseContract): PlayingMovieDao
            = allMoviesDatabase.providePlayingMoviesDao()


    @[Provides AllMoviesScope]
    fun providePopularMoviesListRepository(popularMovieDao: PopularMovieDao): PopularMoviesListRepository
            = PopularMoviesListRepository(popularMovieDao)

    @[Provides AllMoviesScope]
    fun provideTopMoviesListRepository(topMovieDao: TopMovieDao): TopMoviesListRepository
            = TopMoviesListRepository(topMovieDao)

    @[Provides AllMoviesScope]
    fun provideUpcomingMoviesListRepository(upcomingMovieDao: UpcomingMovieDao): UpcomingMoviesListRepository
            = UpcomingMoviesListRepository(upcomingMovieDao)

    @[Provides AllMoviesScope]
    fun providePlayingMoviesListRepository(playingMovieDao: PlayingMovieDao): PlayingMoviesListRepository
            = PlayingMoviesListRepository(playingMovieDao)
}

@Module
interface AllMoviesRepositoryModule {

    @[Binds AllMoviesScope]
    fun bindMoviesListRepositoryImplForPopularMovies(popularMoviesRepository: PopularMoviesListRepository): MoviesListRepositoryImpl

    @[Binds AllMoviesScope]
    fun bindMoviesListRepository(allMoviesListRepository: MoviesListRepositoryImpl): MoviesListRepository

    @[Binds AllMoviesScope]
    fun bindPopularMovieDao(popularMovieDao: PopularMovieDao): MovieDao

    @[Binds AllMoviesScope]
    fun bindTopMovieDao(topMovieDao: TopMovieDao): MovieDao

    @[Binds AllMoviesScope]
    fun bindUpcomingMovieDao(upcomingMovieDao: UpcomingMovieDao): MovieDao

    @[Binds AllMoviesScope]
    fun bindPlayingMovieDao(playingMovieDao: PlayingMovieDao): MovieDao
}