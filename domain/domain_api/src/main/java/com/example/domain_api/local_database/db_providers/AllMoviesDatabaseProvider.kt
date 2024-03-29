package com.example.domain_api.local_database.db_providers

import com.example.domain_api.local_database.repository.MoviesListRepository


interface AllMoviesDatabaseProvider {

    fun provideRepository(): MoviesListRepository
}