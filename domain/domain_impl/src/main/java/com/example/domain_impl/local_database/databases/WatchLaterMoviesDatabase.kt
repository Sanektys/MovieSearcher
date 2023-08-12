package com.example.domain_impl.local_database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.domain_api.local_database.db_contracts.WatchLaterDatabaseContract
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie


@Database(version = 1, exportSchema = true, entities = [WatchLaterDatabaseMovie::class])
abstract class WatchLaterMoviesDatabase : RoomDatabase(), WatchLaterDatabaseContract {

    companion object {
        const val DATABASE_NAME = "WatchLaterMovies.db"
    }
}