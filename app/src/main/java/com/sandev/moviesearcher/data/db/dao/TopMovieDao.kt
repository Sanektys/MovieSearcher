package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TopDatabaseMovie
import io.reactivex.rxjava3.core.Observable


@Dao
abstract class TopMovieDao : MovieDao {

    @Query("SELECT * FROM ${TopDatabaseMovie.TABLE_NAME}")
    abstract override fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    @Query("SELECT *" +
            "FROM " +
            "(SELECT * " +
            "FROM ${TopDatabaseMovie.TABLE_NAME} " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC")
    abstract override fun getLastFewCachedMovies(moviesCount: Int): LiveData<List<DatabaseMovie>>

    @Query("SELECT * FROM ${TopDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    abstract override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("SELECT * " +
            "FROM ${TopDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC")
    abstract override fun getAllSearchedCachedMovies(query: String): LiveData<List<DatabaseMovie>>

    @Query("SELECT * " +
            "FROM" +
            "(SELECT * " +
            "FROM ${TopDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} DESC " +
            "LIMIT :moviesCount) AS q " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC")
    abstract override fun getLastFewSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<DatabaseMovie>>

    @Query("SELECT * " +
            "FROM ${TopDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    abstract override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("INSERT OR IGNORE INTO ${TopDatabaseMovie.TABLE_NAME}" +
            "(${DatabaseMovie.COLUMN_POSTER}, ${DatabaseMovie.COLUMN_TITLE}, " +
            "${DatabaseMovie.COLUMN_DESCRIPTION}, ${DatabaseMovie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    abstract override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("DELETE FROM ${TopDatabaseMovie.TABLE_NAME}")
    abstract override fun deleteAllCachedMovies(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun putToCachedMovies(list: List<TopDatabaseMovie>)

    @Transaction
    open fun deleteAllCachedMoviesAndPutNewMovies(list: List<TopDatabaseMovie>) {
        deleteAllCachedMovies()
        putToCachedMovies(list)
    }
}