package com.example.domain_api.local_database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.UpcomingDatabaseMovie
import io.reactivex.rxjava3.core.Observable


@Dao
abstract class UpcomingMovieDao : MovieDao {

    @Query("SELECT * FROM ${UpcomingDatabaseMovie.TABLE_NAME}")
    abstract override fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    @Query("SELECT * FROM ${UpcomingDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    abstract override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("SELECT * " +
            "FROM ${UpcomingDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    abstract override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("INSERT OR IGNORE INTO ${UpcomingDatabaseMovie.TABLE_NAME}" +
            "(${DatabaseMovie.COLUMN_POSTER}, ${DatabaseMovie.COLUMN_TITLE}, " +
            "${DatabaseMovie.COLUMN_DESCRIPTION}, ${DatabaseMovie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    abstract override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("DELETE FROM ${UpcomingDatabaseMovie.TABLE_NAME}")
    abstract override fun deleteAllCachedMovies(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun putToCachedMovies(list: List<UpcomingDatabaseMovie>)

    @Transaction
    open fun deleteAllCachedMoviesAndPutNewMovies(list: List<UpcomingDatabaseMovie>) {
        deleteAllCachedMovies()
        putToCachedMovies(list)
    }
}