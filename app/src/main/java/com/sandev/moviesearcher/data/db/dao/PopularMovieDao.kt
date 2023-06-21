package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.PopularMovie


@Dao
interface PopularMovieDao : MovieDao {

    @Query("SELECT * FROM ${PopularMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM ${PopularMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String): LiveData<List<Movie>>

    @Query("INSERT OR IGNORE INTO ${PopularMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("DELETE FROM ${PopularMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}