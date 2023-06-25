package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.UpcomingMovie


@Dao
abstract class UpcomingMovieDao : MovieDao {

    @Query("SELECT * FROM ${UpcomingMovie.TABLE_NAME}")
    abstract override fun getAllCachedMovies(): LiveData<List<Movie>>

    @Query("SELECT *" +
            "FROM " +
            "(SELECT * " +
            "FROM ${UpcomingMovie.TABLE_NAME} " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    abstract override fun getLastFewCachedMovies(moviesCount: Int): LiveData<List<Movie>>

    @Query("SELECT * FROM ${UpcomingMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    abstract override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<Movie>

    @Query("SELECT * " +
            "FROM ${UpcomingMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    abstract override fun getAllSearchedCachedMovies(query: String): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM" +
            "(SELECT * " +
            "FROM ${UpcomingMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    abstract override fun getLastFewSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM ${UpcomingMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    abstract override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<Movie>

    @Query("INSERT OR IGNORE INTO ${UpcomingMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    abstract override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("DELETE FROM ${UpcomingMovie.TABLE_NAME}")
    abstract override fun deleteAllCachedMovies(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun putToCachedMovies(list: List<UpcomingMovie>)

    @Transaction
    open fun deleteAllCachedMoviesAndPutNewMovies(list: List<UpcomingMovie>) {
        deleteAllCachedMovies()
        putToCachedMovies(list)
    }
}