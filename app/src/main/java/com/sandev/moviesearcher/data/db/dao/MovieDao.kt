package com.sandev.moviesearcher.data.db.dao

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import io.reactivex.rxjava3.core.Observable


interface MovieDao {

    fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    fun getLastFewCachedMovies(moviesCount: Int): LiveData<List<DatabaseMovie>>

    fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getAllSearchedCachedMovies(query: String): LiveData<List<DatabaseMovie>>

    fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getLastFewSearchedCachedMovies(query: String, moviesCount: Int): LiveData<List<DatabaseMovie>>

    fun putToCachedMovies(poster: String?, title: String, description: String, rating: Float): Long

    fun deleteAllCachedMovies(): Int
}