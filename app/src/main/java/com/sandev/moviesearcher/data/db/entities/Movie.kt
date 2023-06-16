package com.sandev.moviesearcher.data.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Parcelize
open class Movie(
    @[ColumnInfo(name = COLUMN_ID) PrimaryKey(autoGenerate = true)]
    open val id: Int = 0,
    @ColumnInfo(name = COLUMN_POSTER)
    open val poster: String?,
    @ColumnInfo(name = COLUMN_TITLE, index = true, defaultValue = DEFAULT_TITLE)
    open val title: String,
    @ColumnInfo(name = COLUMN_DESCRIPTION, defaultValue = "")
    open val description: String,
    @ColumnInfo(name = COLUMN_RATING, defaultValue = "0")
    open var rating: Float = 0f
): Parcelable {

    companion object {
        const val COLUMN_ID = "_id"
        const val COLUMN_POSTER = "poster"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_RATING = "rating"

        const val DEFAULT_TITLE = "no title"
    }
}