package com.sandev.moviesearcher.data.db.entities

import android.os.Parcelable


interface Movie : Parcelable {

    val id: Int
    val poster: String?
    val title: String
    val description: String
    var rating: Float

    companion object {
        const val COLUMN_ID = "_id"
        const val COLUMN_POSTER = "poster"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_RATING = "rating"

        const val DEFAULT_TITLE = "no title"
    }
}