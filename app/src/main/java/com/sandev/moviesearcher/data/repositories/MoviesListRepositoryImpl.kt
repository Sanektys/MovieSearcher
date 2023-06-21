package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import java.util.concurrent.Executors


open class MoviesListRepositoryImpl(protected val movieDao: MovieDao) : MoviesListRepository {

    override fun putToDB(movies: List<Movie>) {
        Executors.newSingleThreadExecutor().execute {
            movies.forEach { movie ->
                movieDao.putToCachedMovies(
                    poster = movie.poster,
                    title = movie.title,
                    description = movie.description,
                    rating = movie.rating
                )
            }
        }
    }

    override fun getAllFromDB(): LiveData<List<Movie>> = movieDao.getAllCachedMovies()

    override fun getSearchedFromDB(query: String): LiveData<List<Movie>> = movieDao.getSearchedCachedMovies(query)

    override fun deleteAllFromDB() {
        Executors.newSingleThreadExecutor().execute {
            movieDao.deleteAllCachedMovies()
        }
    }
}