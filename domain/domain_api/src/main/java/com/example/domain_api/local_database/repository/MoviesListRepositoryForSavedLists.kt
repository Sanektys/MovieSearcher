package com.example.domain_api.local_database.repository

import com.example.domain_api.local_database.entities.DatabaseMovie


interface MoviesListRepositoryForSavedLists : MoviesListRepository {

    fun deleteFromDB(databaseMovie: DatabaseMovie)
}