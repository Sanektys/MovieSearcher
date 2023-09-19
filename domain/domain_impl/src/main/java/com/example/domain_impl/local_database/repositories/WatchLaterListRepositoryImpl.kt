package com.example.domain_impl.local_database.repositories

import com.example.domain_api.local_database.daos.WatchLaterMovieDao
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.example.domain_api.local_database.repository.WatchLaterListRepository
import io.reactivex.rxjava3.core.Observable


class WatchLaterListRepositoryImpl(private val watchLaterMovieDao: WatchLaterMovieDao)
    : MoviesListRepositoryForSavedListsImpl(watchLaterMovieDao), WatchLaterListRepository {

    override fun putToDB(movies: List<DatabaseMovie>) {
        movies.forEach { databaseMovie ->
            databaseMovie as WatchLaterDatabaseMovie

            watchLaterMovieDao.putToCachedMovies(
                poster = databaseMovie.poster, title = databaseMovie.title,
                description = databaseMovie.description, rating = databaseMovie.rating,
                notificationDate = databaseMovie.notificationDate
            )
        }
    }

    override fun getAllWatchMoviesLaterFromDB(): Observable<List<WatchLaterDatabaseMovie>>
            = watchLaterMovieDao.getAllWatchLaterCachedMovies()

    override fun getWatchLaterMoviesFromDB(from: Int, moviesCount: Int): List<WatchLaterDatabaseMovie>
            = watchLaterMovieDao.getFewWatchLaterMoviesFromOffset(from, moviesCount)

    override fun getSearchedWatchLaterMovieFromDB(
        query: String,
        from: Int,
        moviesCount: Int
    ): List<WatchLaterDatabaseMovie>
            = watchLaterMovieDao.getFewSearchedWatchLaterMoviesFromOffset(query, from, moviesCount)

    override fun updateNotificationDate(movie: WatchLaterDatabaseMovie) {
        watchLaterMovieDao.setNotificationDate(
            title = movie.title,
            description = movie.description,
            notificationDate = movie.notificationDate ?: return
        )
    }
}