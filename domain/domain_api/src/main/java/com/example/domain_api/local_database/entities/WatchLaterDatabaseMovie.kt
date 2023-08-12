package com.example.domain_api.local_database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = WatchLaterDatabaseMovie.TABLE_NAME,
    indices = [Index(value = [DatabaseMovie.COLUMN_TITLE, DatabaseMovie.COLUMN_DESCRIPTION], unique = true)])
data class WatchLaterDatabaseMovie(
    @[ColumnInfo(name = DatabaseMovie.COLUMN_ID) PrimaryKey(autoGenerate = true)]
    override val id: Int = 0,
    @ColumnInfo(name = DatabaseMovie.COLUMN_POSTER)
    override val poster: String?,
    @ColumnInfo(name = DatabaseMovie.COLUMN_TITLE, defaultValue = DatabaseMovie.DEFAULT_TITLE)
    override val title: String,
    @ColumnInfo(name = DatabaseMovie.COLUMN_DESCRIPTION, defaultValue = "")
    override val description: String,
    @ColumnInfo(name = DatabaseMovie.COLUMN_RATING, defaultValue = "0")
    override var rating: Float = 0f
) :  DatabaseMovie(id, poster, title, description, rating) {

    companion object {
        const val TABLE_NAME = "cached_watch_later_movies"
    }
}