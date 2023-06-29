package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.dao.SavedMovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription
import java.util.concurrent.Executors


class MoviesListRepositoryImplForSavedLists(private val savedMovieDao: SavedMovieDao)
    : MoviesListRepositoryImpl(savedMovieDao) {

    fun deleteFromDB(movie: Movie) {
        Executors.newSingleThreadExecutor().execute {
            savedMovieDao.deleteFromCachedMovies(
                TitleAndDescription(title = movie.title, description = movie.description)
            )
        }
    }
}