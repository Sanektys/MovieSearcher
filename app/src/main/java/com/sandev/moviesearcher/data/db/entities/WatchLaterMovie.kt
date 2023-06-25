package com.sandev.moviesearcher.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = WatchLaterMovie.TABLE_NAME,
    indices = [Index(value = [Movie.COLUMN_TITLE, Movie.COLUMN_DESCRIPTION], unique = true)])
data class WatchLaterMovie(
    @[ColumnInfo(name = Movie.COLUMN_ID) PrimaryKey(autoGenerate = true)]
    override val id: Int = 0,
    @ColumnInfo(name = Movie.COLUMN_POSTER)
    override val poster: String?,
    @ColumnInfo(name = Movie.COLUMN_TITLE, defaultValue = Movie.DEFAULT_TITLE)
    override val title: String,
    @ColumnInfo(name = Movie.COLUMN_DESCRIPTION, defaultValue = "")
    override val description: String,
    @ColumnInfo(name = Movie.COLUMN_RATING, defaultValue = "0")
    override var rating: Float = 0f
) :  Movie(id, poster, title, description, rating) {

    companion object {
        const val TABLE_NAME = "cached_watch_later_movies"
    }
}