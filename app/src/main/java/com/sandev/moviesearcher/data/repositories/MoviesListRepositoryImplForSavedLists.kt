package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.SavedMovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription
import java.util.concurrent.Executors


class MoviesListRepositoryImplForSavedLists(moviesDao: MovieDao) : MoviesListRepositoryImpl(moviesDao) {

    fun deleteFromDB(movie: Movie) {
        Executors.newSingleThreadExecutor().execute {
            (movieDao as SavedMovieDao).deleteFromCachedMovies(
                TitleAndDescription(title = movie.title, description = movie.description)
            )
        }
    }
}