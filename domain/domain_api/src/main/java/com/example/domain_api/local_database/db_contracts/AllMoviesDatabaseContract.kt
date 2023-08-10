package com.example.domain_api.local_database.db_contracts

import com.example.domain_api.local_database.repository.MoviesListRepository


interface AllMoviesDatabaseContract {

    fun provideRepository(): MoviesListRepository
}