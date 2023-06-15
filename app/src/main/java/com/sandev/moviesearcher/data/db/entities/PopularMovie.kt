package com.sandev.moviesearcher.data.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = PopularMovie.TABLE_NAME, indices = [Index(value = [Movie.COLUMN_DESCRIPTION], unique = true)])
data class PopularMovie(
    @[ColumnInfo(name = Movie.COLUMN_ID) PrimaryKey(autoGenerate = true)]
    override val id: Int = 0,
    @ColumnInfo(name = Movie.COLUMN_POSTER)
    override val poster: String?,
    @ColumnInfo(name = Movie.COLUMN_TITLE, index = true, defaultValue = Movie.DEFAULT_TITLE)
    override val title: String,
    @ColumnInfo(name = Movie.COLUMN_DESCRIPTION, defaultValue = "")
    override val description: String,
    @ColumnInfo(name = Movie.COLUMN_RATING, defaultValue = "0")
    override var rating: Float = 0f
) : Movie {

    companion object {
        const val TABLE_NAME = "cached_popular_movies"
    }
}
