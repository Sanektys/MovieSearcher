package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.Movie


interface MovieDao {

    fun getAllCachedMovies(): LiveData<List<Movie>>

    fun getCachedMovies(moviesCount: Int): LiveData<List<Movie>>

    fun getSearchedCachedMovies(query: String): LiveData<List<Movie>>

    fun getSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<Movie>>

    fun putToCachedMovies(poster: String?, title: String, description: String, rating: Float): Long

    fun deleteAllCachedMovies(): Int
}