package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.domain.Movie


interface MoviesListRepository {

    fun putToDB(movie: Movie)

    fun getAllFromDB(): List<Movie>

    fun getSearchedFromDB(query: String): List<Movie>
}