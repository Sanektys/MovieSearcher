package com.sandev.moviesearcher.data.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.sandev.moviesearcher.data.db.MoviesDatabase
import com.sandev.moviesearcher.domain.Movie


open class MoviesListRepositoryImpl(protected val moviesDatabase: MoviesDatabase) : MoviesListRepository {

    protected val sqlDB: SQLiteDatabase = moviesDatabase.readableDatabase


    override fun putToDB(movie: Movie): Long {
        val cv = ContentValues().apply {
            put(MoviesDatabase.COLUMN_TITLE, movie.title)
            put(MoviesDatabase.COLUMN_DESCRIPTION, movie.description)
            put(MoviesDatabase.COLUMN_POSTER, movie.poster)
            put(MoviesDatabase.COLUMN_RATING, movie.rating)
        }
        return sqlDB.insertWithOnConflict(moviesDatabase.getTableName(), null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    override fun getAllFromDB(): List<Movie> {
        return sqlDB.rawQuery(
            "SELECT * " +
                    "FROM ${moviesDatabase.getTableName()}",
            null
        ).use { cursor -> readResultTable(cursor) }
    }

    override fun getSearchedFromDB(query: String): List<Movie> {
        return sqlDB.rawQuery(
            "SELECT * " +
                    "FROM ${moviesDatabase.getTableName()} " +
                    "WHERE ${MoviesDatabase.COLUMN_TITLE} LIKE '%${query.lowercase()}%' " +
                    "ORDER BY ${MoviesDatabase.COLUMN_ID} ASC",
            null
        ).use { cursor -> readResultTable(cursor) }
    }

    private fun readResultTable(cursor: Cursor): List<Movie> {
        val result = mutableListOf<Movie>()

        if (cursor.moveToFirst()) {
            do {
                val movie = Movie(
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MoviesDatabase.COLUMN_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(MoviesDatabase.COLUMN_DESCRIPTION)),
                    poster = cursor.getString(cursor.getColumnIndexOrThrow(MoviesDatabase.COLUMN_POSTER)),
                    rating = cursor.getFloat(cursor.getColumnIndexOrThrow(MoviesDatabase.COLUMN_RATING))
                )
                result.add(movie)
            } while (cursor.moveToNext())
        }

        return result
    }
}