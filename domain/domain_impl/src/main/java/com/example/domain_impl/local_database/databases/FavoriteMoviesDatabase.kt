package com.example.domain_impl.local_database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.domain_api.local_database.db_contracts.FavoriteDatabaseContract
import com.sandev.moviesearcher.data.db.entities.FavoriteDatabaseMovie


@Database(version = 1, exportSchema = true, entities = [FavoriteDatabaseMovie::class])
abstract class FavoriteMoviesDatabase : RoomDatabase(), FavoriteDatabaseContract {

    companion object {
        const val DATABASE_NAME = "FavoriteMovies.db"
    }
}