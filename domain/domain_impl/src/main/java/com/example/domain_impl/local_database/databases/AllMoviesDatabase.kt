package com.example.domain_impl.local_database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.domain_api.local_database.db_contracts.AllMoviesDatabaseContract
import com.sandev.moviesearcher.data.db.entities.PlayingDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.PopularDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TopDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingDatabaseMovie


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