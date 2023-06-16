package com.sandev.moviesearcher.data.db.dao

import com.sandev.moviesearcher.data.db.entities.Movie


interface MovieDao {

    fun getAllCachedMovies(): List<Movie>

    fun getSearchedCachedMovies(query: String): List<Movie>

    fun putToCachedMovies(poster: String?, title: String, description: String, rating: Float): Long

    fun deleteAllCachedMovies(): Int
}