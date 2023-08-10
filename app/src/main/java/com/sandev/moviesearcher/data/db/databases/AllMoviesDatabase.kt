package com.sandev.moviesearcher.data.db.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandev.moviesearcher.data.db.dao.PlayingMovieDao
import com.sandev.moviesearcher.data.db.dao.PopularMovieDao
import com.sandev.moviesearcher.data.db.dao.TopMovieDao
import com.sandev.moviesearcher.data.db.dao.UpcomingMovieDao
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
abstract class AllMoviesDatabase : RoomDatabase() {

    abstract fun popularMovieDao(): PopularMovieDao
    
    abstract fun topMovieDao(): TopMovieDao

    abstract fun upcomingMovieDao(): UpcomingMovieDao

    abstract fun playingMovieDao(): PlayingMovieDao


    companion object {
        const val DATABASE_NAME = "AllMovies.db"
    }
}