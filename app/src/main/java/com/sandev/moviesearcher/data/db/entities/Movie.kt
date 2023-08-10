package com.sandev.moviesearcher.data.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Objects


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

    final override fun equals(other: Any?): Boolean {
        if (other == null || other !is DatabaseMovie) return false

        if (other === this) return true

        // В проверке не участвует ID
        return Objects.equals(this.poster, other.poster) &&
                Objects.equals(this.title, other.title) &&
                Objects.equals(this.description, other.description) &&
                Objects.equals(this.rating, other.rating)
    }

    final override fun hashCode(): Int {
        return Objects.hash(poster, title, description, rating)
    }

    companion object {
        const val COLUMN_ID = "_id"
        const val COLUMN_POSTER = "poster"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_RATING = "rating"

        const val DEFAULT_TITLE = "no title"
    }
}