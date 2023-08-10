package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.FavoriteDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription
import io.reactivex.rxjava3.core.Observable


@Dao
interface FavoriteMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${FavoriteDatabaseMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    @Query("SELECT *" +
            "FROM " +
            "(SELECT * " +
            "FROM ${FavoriteDatabaseMovie.TABLE_NAME} " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getLastFewCachedMovies(moviesCount: Int): LiveData<List<DatabaseMovie>>

    @Query("SELECT * FROM ${FavoriteDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("SELECT * " +
            "FROM ${FavoriteDatabaseMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getAllSearchedCachedMovies(query: String): LiveData<List<DatabaseMovie>>

    @Query("SELECT * " +
            "FROM" +
            "(SELECT * " +
            "FROM ${FavoriteDatabaseMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getLastFewSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<DatabaseMovie>>

    @Query("SELECT * " +
            "FROM ${FavoriteDatabaseMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("INSERT OR IGNORE INTO ${FavoriteDatabaseMovie.TABLE_NAME}" +
            "(${Movie.COLUMN_POSTER}, ${Movie.COLUMN_TITLE}, " +
            "${Movie.COLUMN_DESCRIPTION}, ${Movie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Delete(entity = FavoriteDatabaseMovie::class)
    override fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int

    @Query("DELETE FROM ${FavoriteDatabaseMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}