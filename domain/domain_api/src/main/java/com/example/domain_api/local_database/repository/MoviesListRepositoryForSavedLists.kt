package com.example.domain_api.local_database.repository

import com.sandev.moviesearcher.data.db.entities.DatabaseMovie


interface MoviesListRepositoryForSavedLists {

    fun deleteFromDB(databaseMovie: DatabaseMovie)
}