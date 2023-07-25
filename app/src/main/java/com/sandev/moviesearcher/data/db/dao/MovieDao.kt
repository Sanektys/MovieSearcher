package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.Movie
import io.reactivex.rxjava3.core.Observable


interface MovieDao {

    fun getAllCachedMovies(): Observable<List<Movie>>

    fun getLastFewCachedMovies(moviesCount: Int): LiveData<List<Movie>>

    fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<Movie>

    fun getAllSearchedCachedMovies(query: String): LiveData<List<Movie>>

    fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<Movie>

    fun getLastFewSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<Movie>>

    fun putToCachedMovies(poster: String?, title: String, description: String, rating: Float): Long

    fun deleteAllCachedMovies(): Int
}