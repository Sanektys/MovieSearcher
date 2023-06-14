package com.sandev.moviesearcher.data.db.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sandev.moviesearcher.data.db.dao.PlayingMovieDao
import com.sandev.moviesearcher.data.db.dao.PopularMovieDao
import com.sandev.moviesearcher.data.db.dao.TopMovieDao
import com.sandev.moviesearcher.data.db.dao.UpcomingMovieDao
import com.sandev.moviesearcher.data.db.entities.PlayingMovie
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.data.db.entities.TopMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingMovie


@Database(version = 1, exportSchema = true, entities = [
    PopularMovie::class,
    TopMovie::class,
    UpcomingMovie::class,
    PlayingMovie::class
])
abstract class AllMoviesDatabase : RoomDatabase() {

    abstract fun popularMovieDao(): PopularMovieDao
    
    abstract fun topMovieDao(): TopMovieDao

    abstract fun upcomingMovieDao(): UpcomingMovieDao

    abstract fun playingMovieDao(): PlayingMovieDao
}