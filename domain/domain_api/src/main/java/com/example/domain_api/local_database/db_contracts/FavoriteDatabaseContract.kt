package com.example.domain_api.local_database.db_contracts

import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists


interface FavoriteDatabaseContract {

    fun provideRepository(): MoviesListRepositoryForSavedLists
}