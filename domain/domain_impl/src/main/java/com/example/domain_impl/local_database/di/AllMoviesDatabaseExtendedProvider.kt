package com.example.domain_impl.local_database.di

import com.example.domain_api.local_database.db_providers.AllMoviesDatabaseProvider
import com.example.domain_impl.local_database.repositories.PlayingMoviesListRepository
import com.example.domain_impl.local_database.repositories.PopularMoviesListRepository
import com.example.domain_impl.local_database.repositories.TopMoviesListRepository
import com.example.domain_impl.local_database.repositories.UpcomingMoviesListRepository


interface AllMoviesDatabaseExtendedProvider : AllMoviesDatabaseProvider {

    fun provideTopMoviesRepository(): TopMoviesListRepository

    fun providePopularMoviesRepository(): PopularMoviesListRepository

    fun provideUpcomingMoviesRepository(): UpcomingMoviesListRepository

    fun providePlayingMoviesRepository(): PlayingMoviesListRepository
}