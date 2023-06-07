package com.sandev.moviesearcher.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase


class FavoriteMoviesDatabase(context: Context) : MoviesDatabase(context, DATABASE_NAME, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_TITLE TEXT NOT NULL, " +
                    "$COLUMN_DESCRIPTION TEXT UNIQUE, " +
                    "$COLUMN_POSTER TEXT, " +
                    "$COLUMN_RATING REAL" +
                    ")"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }

    override fun getTableName() = TABLE_NAME


    companion object {
        const val DATABASE_NAME = "FavoriteMovies.db"
        const val DATABASE_VERSION = 1

        const val TABLE_NAME = "favorite_movies"
    }
}