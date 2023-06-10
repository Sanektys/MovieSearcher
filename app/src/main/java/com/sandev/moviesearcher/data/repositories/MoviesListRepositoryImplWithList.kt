package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.MoviesDatabase
import com.sandev.moviesearcher.domain.Movie


class MoviesListRepositoryImplWithList(moviesDatabase: MoviesDatabase)
    : MoviesListRepositoryImpl(moviesDatabase) {

    private val moviesList = mutableListOf<Movie>()
    private var moviesCountInDB: Int = 0

    init {
        moviesList.addAll(super.getAllFromDB())
        moviesCountInDB = moviesList.size
    }


    override fun putToDB(movie: Movie): Long {
        val result = super.putToDB(movie)
        if (result != -1L) {
            ++moviesCountInDB
        }
        moviesList.add(movie)

        return result
    }

    override fun getAllFromDB(): List<Movie> = moviesList.toList()

    fun deleteFromDB(movie: Movie) {
        val numberOfDeletions = sqlDB.delete(
            moviesDatabase.getTableName(),
            "${MoviesDatabase.COLUMN_TITLE}=? AND ${MoviesDatabase.COLUMN_DESCRIPTION}=?",
            arrayOf(movie.title, movie.description)
        )
        moviesCountInDB -= numberOfDeletions

        moviesList.remove(movie)
    }

    fun getMoviesCountInList() = moviesList.size

    fun getMoviesCountInDB() = moviesCountInDB
}