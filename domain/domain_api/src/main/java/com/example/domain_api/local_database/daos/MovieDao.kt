package com.example.domain_api.local_database.daos

import com.example.domain_api.local_database.entities.DatabaseMovie
import io.reactivex.rxjava3.core.Observable


interface MovieDao {

    fun getAllCachedMovies(): Observable<List<DatabaseMovie>>

    fun getFewCachedMoviesFromOffset(from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getFewSearchedCachedMoviesFromOffset(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    fun putToCachedMovies(poster: String?, title: String, description: String, rating: Float): Long

    fun deleteAllCachedMovies(): Int
}