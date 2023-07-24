package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.dao.SavedMovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


class MoviesListRepositoryImplForSavedLists(private val savedMovieDao: SavedMovieDao)
    : MoviesListRepositoryImpl(savedMovieDao) {

    fun deleteFromDB(movie: Movie) {
        savedMovieDao.deleteFromCachedMovies(
            TitleAndDescription(title = movie.title, description = movie.description)
        )
    }
}