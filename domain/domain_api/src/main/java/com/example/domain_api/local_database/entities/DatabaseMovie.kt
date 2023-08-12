package com.example.domain_api.local_database.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.example.domain_api.dto.Movie
import kotlinx.parcelize.Parcelize
import java.util.Objects


@Parcelize
open class DatabaseMovie(
    @[ColumnInfo(name = COLUMN_ID) PrimaryKey(autoGenerate = true)]
    override val id: Int = 0,
    @ColumnInfo(name = COLUMN_POSTER)
    override val poster: String?,
    @ColumnInfo(name = COLUMN_TITLE, index = true, defaultValue = DEFAULT_TITLE)
    override val title: String,
    @ColumnInfo(name = COLUMN_DESCRIPTION, defaultValue = "")
    override val description: String,
    @ColumnInfo(name = COLUMN_RATING, defaultValue = "0")
    override var rating: Float = 0f
): Parcelable, Movie {

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