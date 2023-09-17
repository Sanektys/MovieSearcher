package com.example.domain_api.local_database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.FavoriteDatabaseMovie
import com.example.domain_api.local_database.entities.TitleAndDescription
import io.reactivex.rxjava3.core.Observable


@Dao
interface FavoriteMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${FavoriteDatabaseMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    @Query("SELECT * FROM ${FavoriteDatabaseMovie.TABLE_NAME} LIMIT :from, :moviesCount")
    override fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("SELECT * " +
            "FROM ${FavoriteDatabaseMovie.TABLE_NAME} " +
            "WHERE ${DatabaseMovie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${DatabaseMovie.COLUMN_ID} ASC " +
            "LIMIT :from, :moviesCount")
    override fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    @Query("INSERT OR IGNORE INTO ${FavoriteDatabaseMovie.TABLE_NAME}" +
            "(${DatabaseMovie.COLUMN_POSTER}, ${DatabaseMovie.COLUMN_TITLE}, " +
            "${DatabaseMovie.COLUMN_DESCRIPTION}, ${DatabaseMovie.COLUMN_RATING}) " +
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