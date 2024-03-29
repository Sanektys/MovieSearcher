package com.example.domain_impl.local_database.repositories

import com.example.domain_api.local_database.daos.SavedMovieDao
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.TitleAndDescription
import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists


open class MoviesListRepositoryForSavedListsImpl(private val savedMovieDao: SavedMovieDao)
    : MoviesListRepositoryImpl(savedMovieDao), MoviesListRepositoryForSavedLists {

    override fun deleteFromDB(databaseMovie: DatabaseMovie) {
        savedMovieDao.deleteFromCachedMovies(
            TitleAndDescription(title = databaseMovie.title, description = databaseMovie.description)
        )
    }
}