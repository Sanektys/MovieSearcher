package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription
import com.sandev.moviesearcher.data.db.entities.WatchLaterMovie


@Dao
interface WatchLaterMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${WatchLaterMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): LiveData<List<Movie>>

    @Query("SELECT *" +
            "FROM " +
            "(SELECT * " +
            "FROM ${WatchLaterMovie.TABLE_NAME} " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getCachedMovies(moviesCount: Int): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM ${WatchLaterMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM" +
            "(SELECT * " +
            "FROM ${WatchLaterMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<Movie>>

    @Query("INSERT OR IGNORE INTO ${WatchLaterMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Delete(entity = WatchLaterMovie::class)
    override fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int

    @Query("DELETE FROM ${WatchLaterMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}