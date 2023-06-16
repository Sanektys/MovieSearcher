package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.entities.Movie


interface MoviesListRepository {

    fun putToDB(movies: List<Movie>)

    fun getAllFromDB(): List<Movie>

    fun getSearchedFromDB(query: String): List<Movie>

    fun deleteAllFromDB()
}