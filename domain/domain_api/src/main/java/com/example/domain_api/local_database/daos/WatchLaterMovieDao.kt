package com.example.domain_api.local_database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Update
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.TitleAndDescription
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import io.reactivex.rxjava3.core.Observable


@Dao
interface WatchLaterMovieDao : SavedMovieDao {

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM ${WatchLaterDatabaseMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    @Query("SELECT * FROM ${WatchLaterDatabaseMovie.TABLE_NAME}")
    fun getAllWatchLaterCachedMovies(): Observable<List<WatchLaterDatabaseMovie>>

    @Query("SELECT * FROM ${WatchLaterDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    fun getFewWatchLaterMoviesFromOffset(from: Int, moviesCount: Int): List<WatchLaterDatabaseMovie>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM ${WatchLaterDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("SELECT * " +
            "FROM ${WatchLaterDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    fun getFewSearchedWatchLaterMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<WatchLaterDatabaseMovie>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * " +
            "FROM ${WatchLaterDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("INSERT OR IGNORE INTO ${WatchLaterDatabaseMovie.TABLE_NAME}" +
            "(${DatabaseMovie.COLUMN_POSTER}, ${DatabaseMovie.COLUMN_TITLE}, " +
            "${DatabaseMovie.COLUMN_DESCRIPTION}, ${DatabaseMovie.COLUMN_RATING}) " +
            "VALUES (:poster, :title, :description, :rating)")
    override fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float
    ): Long

    @Query("INSERT OR IGNORE INTO ${WatchLaterDatabaseMovie.TABLE_NAME}" +
            "(${DatabaseMovie.COLUMN_POSTER}, ${DatabaseMovie.COLUMN_TITLE}, " +
            "${DatabaseMovie.COLUMN_DESCRIPTION}, ${DatabaseMovie.COLUMN_RATING}, " +
            "${WatchLaterDatabaseMovie.COLUMN_NOTIFICATION_DATE}) " +
            "VALUES (:poster, :title, :description, :rating, :notificationDate)")
    fun putToCachedMovies(
        poster: String?,
        title: String,
        description: String,
        rating: Float,
        notificationDate: Long?
    ): Long

    @Query("UPDATE ${WatchLaterDatabaseMovie.TABLE_NAME} " +
            "SET ${WatchLaterDatabaseMovie.COLUMN_NOTIFICATION_DATE}=:notificationDate  " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE}=:title AND ${DatabaseMovie.COLUMN_DESCRIPTION}=:description")
    fun setNotificationDate(title: String, description: String, notificationDate: Long)

    @Delete(entity = WatchLaterDatabaseMovie::class)
    override fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int

    @Query("DELETE FROM ${WatchLaterDatabaseMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}