package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.PlayingMovieDao
import com.sandev.moviesearcher.data.db.dao.PopularMovieDao
import com.sandev.moviesearcher.data.db.dao.TopMovieDao
import com.sandev.moviesearcher.data.db.dao.UpcomingMovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.PlayingMovie
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.data.db.entities.TopMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingMovie
import java.util.concurrent.Executors


open class MoviesListRepositoryImpl(private val movieDao: MovieDao) : MoviesListRepository {

    @Suppress("UNCHECKED_CAST")
    override fun putToDB(movies: List<Movie>) {
        Executors.newSingleThreadExecutor().execute {
            when (movieDao) {
                is PlayingMovieDao  -> movieDao.putToCachedMovies(movies as List<PlayingMovie>)
                is PopularMovieDao  -> movieDao.putToCachedMovies(movies as List<PopularMovie>)
                is TopMovieDao      -> movieDao.putToCachedMovies(movies as List<TopMovie>)
                is UpcomingMovieDao -> movieDao.putToCachedMovies(movies as List<UpcomingMovie>)
            }
        }
    }

    override fun getAllFromDB(): LiveData<List<Movie>> = movieDao.getAllCachedMovies()

    override fun getFromDB(moviesCount: Int): LiveData<List<Movie>>
            = movieDao.getCachedMovies(moviesCount)

    override fun getFromDB(from: Int, moviesCount: Int): List<Movie>
            = movieDao.getCachedMovies(from, moviesCount)

    override fun getSearchedFromDB(query: String): LiveData<List<Movie>> = movieDao.getSearchedCachedMovies(query)

    override fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<Movie>
            = movieDao.getSearchedCachedMovies(query, from, moviesCount)

    override fun getSearchedFromDB(query: String, moviesCount: Int): LiveData<List<Movie>>
            = movieDao.getSearchedCachedMovies(query, moviesCount)

    override fun deleteAllFromDB() {
        Executors.newSingleThreadExecutor().execute {
            movieDao.deleteAllCachedMovies()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun deleteAllFromDBAndPutNew(movies: List<Movie>) {
        Executors.newSingleThreadExecutor().execute {
            when (movieDao) {
                is PlayingMovieDao  -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<PlayingMovie>)
                is PopularMovieDao  -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<PopularMovie>)
                is TopMovieDao      -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<TopMovie>)
                is UpcomingMovieDao -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<UpcomingMovie>)
            }
        }
    }
}