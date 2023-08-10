package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.dao.SavedMovieDao
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


class MoviesListRepositoryImplForSavedLists(private val savedMovieDao: SavedMovieDao)
    : MoviesListRepositoryImpl(savedMovieDao) {

    fun deleteFromDB(databaseMovie: DatabaseMovie) {
        savedMovieDao.deleteFromCachedMovies(
            TitleAndDescription(title = databaseMovie.title, description = databaseMovie.description)
        )
    }
}