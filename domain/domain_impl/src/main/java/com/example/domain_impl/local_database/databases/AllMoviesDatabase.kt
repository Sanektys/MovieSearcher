package com.example.domain_impl.local_database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.domain_api.local_database.db_contracts.AllMoviesDatabaseContract
import com.example.domain_api.local_database.entities.PlayingDatabaseMovie
import com.example.domain_api.local_database.entities.PopularDatabaseMovie
import com.example.domain_api.local_database.entities.TopDatabaseMovie
import com.example.domain_api.local_database.entities.UpcomingDatabaseMovie


@Database(version = 1, exportSchema = true, entities = [
    PopularDatabaseMovie::class,
    TopDatabaseMovie::class,
    UpcomingDatabaseMovie::class,
    PlayingDatabaseMovie::class
])
abstract class AllMoviesDatabase : RoomDatabase(), AllMoviesDatabaseContract {

    companion object {
        const val DATABASE_NAME = "AllMovies.db"
    }
}