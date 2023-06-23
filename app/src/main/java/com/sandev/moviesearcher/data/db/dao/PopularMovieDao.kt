package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.PopularMovie


@Dao
abstract class PopularMovieDao : MovieDao {

    @Query("SELECT * FROM ${PopularMovie.TABLE_NAME}")
    abstract override fun getAllCachedMovies(): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM ${PopularMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    abstract override fun getSearchedCachedMovies(query: String): LiveData<List<Movie>>

    @Query("INSERT OR IGNORE INTO ${PopularMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    abstract override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("DELETE FROM ${PopularMovie.TABLE_NAME}")
    abstract override fun deleteAllCachedMovies(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun putToCachedMovies(list: List<PopularMovie>)

    @Transaction
    open fun deleteAllCachedMoviesAndPutNewMovies(list: List<PopularMovie>) {
        deleteAllCachedMovies()
        putToCachedMovies(list)
    }
}