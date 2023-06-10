package com.sandev.moviesearcher.data.db

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper


abstract class MoviesDatabase(context: Context, databaseName: String, version: Int)
    : SQLiteOpenHelper(context, databaseName, null, version) {

    abstract fun getTableName(): String


    companion object {
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_POSTER = "poster"
        const val COLUMN_RATING = "rating"
    }
}