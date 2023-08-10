package com.sandev.moviesearcher.data.db.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandev.moviesearcher.data.db.dao.FavoriteMovieDao
import com.sandev.moviesearcher.data.db.entities.FavoriteDatabaseMovie


@Database(version = 1, exportSchema = true, entities = [FavoriteDatabaseMovie::class])
abstract class FavoriteMoviesDatabase : RoomDatabase() {

    abstract fun favoriteMovieDao(): FavoriteMovieDao


    companion object {
        const val DATABASE_NAME = "FavoriteMovies.db"
    }
}