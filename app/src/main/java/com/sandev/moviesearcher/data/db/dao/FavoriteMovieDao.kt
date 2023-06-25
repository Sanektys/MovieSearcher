package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.FavoriteMovie
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


@Dao
interface FavoriteMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${FavoriteMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): LiveData<List<Movie>>

    @Query("SELECT *" +
            "FROM " +
            "(SELECT * " +
            "FROM ${FavoriteMovie.TABLE_NAME} " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getCachedMovies(moviesCount: Int): LiveData<List<Movie>>

    @Query("SELECT * FROM ${FavoriteMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    override fun getCachedMovies(from: Int, moviesCount: Int): List<Movie>

    @Query("SELECT * " +
            "FROM ${FavoriteMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM" +
            "(SELECT * " +
            "FROM ${FavoriteMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<Movie>>

    @Query("SELECT * " +
            "FROM ${FavoriteMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    override fun getSearchedCachedMovies(query: String, from: Int, moviesCount: Int): List<Movie>

    @Query("INSERT OR IGNORE INTO ${FavoriteMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Delete(entity = FavoriteMovie::class)
    override fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int

    @Query("DELETE FROM ${FavoriteMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}