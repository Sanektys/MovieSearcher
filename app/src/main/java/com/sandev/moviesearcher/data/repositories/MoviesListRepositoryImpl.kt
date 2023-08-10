package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.dao.FavoriteMovieDao
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.PlayingMovieDao
import com.sandev.moviesearcher.data.db.dao.PopularMovieDao
import com.sandev.moviesearcher.data.db.dao.TopMovieDao
import com.sandev.moviesearcher.data.db.dao.UpcomingMovieDao
import com.sandev.moviesearcher.data.db.dao.WatchLaterMovieDao
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.db.entities.PlayingDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.PopularDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TopDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingDatabaseMovie
import io.reactivex.rxjava3.core.Observable


open class MoviesListRepositoryImpl(private val movieDao: MovieDao) : MoviesListRepository {

    @Suppress("UNCHECKED_CAST")
    override fun putToDB(movies: List<DatabaseMovie>) {
        when (movieDao) {
            is PlayingMovieDao  -> movieDao.putToCachedMovies(movies as List<PlayingDatabaseMovie>)
            is PopularMovieDao  -> movieDao.putToCachedMovies(movies as List<PopularDatabaseMovie>)
            is TopMovieDao      -> movieDao.putToCachedMovies(movies as List<TopDatabaseMovie>)
            is UpcomingMovieDao -> movieDao.putToCachedMovies(movies as List<UpcomingDatabaseMovie>)
            is FavoriteMovieDao, is WatchLaterMovieDao -> {
                movies.forEach {
                    movieDao.putToCachedMovies(
                        poster = it.poster, title = it.title,
                        description = it.description, rating = it.rating
                    )
                }
            }
        }
    }

    override fun getAllFromDB(): Observable<List<DatabaseMovie>>
            = movieDao.getAllCachedMovies()

    override fun getFromDB(moviesCount: Int): LiveData<List<DatabaseMovie>>
            = movieDao.getLastFewCachedMovies(moviesCount)

    override fun getFromDB(from: Int, moviesCount: Int): List<DatabaseMovie>
            = movieDao.getFewCachedMoviesFromOffset(from, moviesCount)

    override fun getSearchedFromDB(query: String): LiveData<List<DatabaseMovie>>
            = movieDao.getAllSearchedCachedMovies(query)

    override fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>
            = movieDao.getFewSearchedCachedMoviesFromOffset(query, from, moviesCount)

    override fun getSearchedFromDB(query: String, moviesCount: Int): LiveData<List<DatabaseMovie>>
            = movieDao.getLastFewSearchedCachedMovies(query, moviesCount)

    override fun deleteAllFromDB() {
        movieDao.deleteAllCachedMovies()
    }

    @Suppress("UNCHECKED_CAST")
    override fun deleteAllFromDBAndPutNew(movies: List<DatabaseMovie>) {
        when (movieDao) {
            is PlayingMovieDao  -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<PlayingDatabaseMovie>)
            is PopularMovieDao  -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<PopularDatabaseMovie>)
            is TopMovieDao      -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<TopDatabaseMovie>)
            is UpcomingMovieDao -> movieDao.deleteAllCachedMoviesAndPutNewMovies(movies as List<UpcomingDatabaseMovie>)
        }
    }
}