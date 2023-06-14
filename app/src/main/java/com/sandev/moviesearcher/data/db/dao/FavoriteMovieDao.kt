package com.sandev.moviesearcher.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sandev.moviesearcher.data.db.entities.FavoritesMovie
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


@Dao
interface FavoriteMovieDao : SavedMovieDao {

    @Query("SELECT * FROM ${FavoritesMovie.TABLE_NAME}")
    override fun getAllCachedMovies(): List<Movie>

    @Query("SELECT * " +
            "FROM ${FavoritesMovie.TABLE_NAME} " +
            "WHERE ${Movie.COLUMN_TITLE} LIKE '%:query%'" +
            "ORDER BY ${Movie.COLUMN_ID} ASC")
    override fun getSearchedCachedMovies(query: String): List<Movie>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override fun putToCachedMovies(movies: List<Movie>): List<Long>

    @Delete(entity = Movie::class)
    override fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int

    @Query("DELETE FROM ${FavoritesMovie.TABLE_NAME}")
    override fun deleteAllCachedMovies(): Int
}