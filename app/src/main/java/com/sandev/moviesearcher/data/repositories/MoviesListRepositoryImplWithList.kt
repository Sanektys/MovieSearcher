package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.MoviesDatabase
import com.sandev.moviesearcher.domain.Movie


class MoviesListRepositoryImplWithList(moviesDatabase: MoviesDatabase)
    : MoviesListRepositoryImpl(moviesDatabase) {

    private val moviesList = mutableListOf<Movie>()

    init {
        moviesList.addAll(super.getAllFromDB())
    }


    override fun putToDB(movie: Movie) {
        super.putToDB(movie)
        moviesList.add(movie)
    }

    override fun getAllFromDB(): List<Movie> = moviesList.toList()

    fun deleteFromDB(movie: Movie) {
        sqlDB.delete(
            moviesDatabase.getTableName(),
            "${MoviesDatabase.COLUMN_TITLE}=? AND ${MoviesDatabase.COLUMN_DESCRIPTION}=?",
            arrayOf(movie.title, movie.description)
        )

        moviesList.remove(movie)
    }
}