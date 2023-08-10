package com.example.domain_api.local_database.db_contracts

import com.example.domain_api.local_database.daos.PlayingMovieDao
import com.example.domain_api.local_database.daos.PopularMovieDao
import com.example.domain_api.local_database.daos.TopMovieDao
import com.example.domain_api.local_database.daos.UpcomingMovieDao


interface AllMoviesDatabaseContract {

    fun provideTopMoviesDao(): TopMovieDao

    fun providePopularMoviesDao(): PopularMovieDao

    fun providePlayingMoviesDao(): PlayingMovieDao

    fun provideUpcomingMoviesDao(): UpcomingMovieDao
}