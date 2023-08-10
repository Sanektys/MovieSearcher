package com.example.domain_api.local_database.db_providers

import com.example.domain_api.local_database.daos.PlayingMovieDao
import com.example.domain_api.local_database.daos.PopularMovieDao
import com.example.domain_api.local_database.daos.TopMovieDao
import com.example.domain_api.local_database.daos.UpcomingMovieDao


interface AllMoviesDatabaseProvider {

    fun provideTopMoviesDao(): TopMovieDao

    fun providePopularMoviesDao(): PopularMovieDao

    fun providePlayingMoviesDao(): PlayingMovieDao

    fun provideUpcomingMoviesDao(): UpcomingMovieDao
}