package com.example.domain_impl.local_database.repositories

import com.example.domain_api.local_database.daos.FavoriteMovieDao
import com.example.domain_api.local_database.daos.MovieDao
import com.example.domain_api.local_database.daos.PlayingMovieDao
import com.example.domain_api.local_database.daos.PopularMovieDao
import com.example.domain_api.local_database.daos.TopMovieDao
import com.example.domain_api.local_database.daos.UpcomingMovieDao
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.PlayingDatabaseMovie
import com.example.domain_api.local_database.entities.PopularDatabaseMovie
import com.example.domain_api.local_database.entities.TopDatabaseMovie
import com.example.domain_api.local_database.entities.UpcomingDatabaseMovie
import com.example.domain_api.local_database.repository.MoviesListRepository
import io.reactivex.rxjava3.core.Observable


open class MoviesListRepositoryImpl(private val movieDao: MovieDao) : MoviesListRepository {

    @Suppress("UNCHECKED_CAST")
    override fun putToDB(movies: List<DatabaseMovie>) {
        when (movieDao) {
            is PlayingMovieDao -> movieDao.putToCachedMovies(movies as List<PlayingDatabaseMovie>)
            is PopularMovieDao -> movieDao.putToCachedMovies(movies as List<PopularDatabaseMovie>)
            is TopMovieDao -> movieDao.putToCachedMovies(movies as List<TopDatabaseMovie>)
            is UpcomingMovieDao -> movieDao.putToCachedMovies(movies as List<UpcomingDatabaseMovie>)
            is FavoriteMovieDao -> {
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

    override fun getFromDB(from: Int, moviesCount: Int): List<DatabaseMovie>
            = movieDao.getFewCachedMoviesFromOffset(from, moviesCount)

    override fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>
            = movieDao.getFewSearchedCachedMoviesFromOffset(query, from, moviesCount)

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