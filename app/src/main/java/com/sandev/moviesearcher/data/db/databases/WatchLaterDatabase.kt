package com.sandev.moviesearcher.data.db.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandev.moviesearcher.data.db.dao.WatchLaterMovieDao
import com.sandev.moviesearcher.data.db.entities.WatchLaterMovie


@Database(version = 1, exportSchema = true, entities = [WatchLaterMovie::class])
abstract class WatchLaterDatabase : RoomDatabase() {

    abstract fun watchLaterMovieDao(): WatchLaterMovieDao
}