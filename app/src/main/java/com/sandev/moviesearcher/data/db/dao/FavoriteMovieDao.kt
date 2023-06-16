package com.sandev.moviesearcher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.FavoriteMovie
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


@Dao
interface FavoriteMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${FavoriteMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): List<Movie>

    @Query("SELECT * " +
            "FROM ${FavoriteMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%' || :query || '%' " +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String): List<Movie>

    //@Insert(onConflict = OnConflictStrategy.IGNORE)
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