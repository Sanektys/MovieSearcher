package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.Movie
import io.reactivex.rxjava3.core.Observable


interface MoviesListRepository {

    fun putToDB(movies: List<Movie>)

    fun getAllFromDB(): Observable<List<Movie>>

    fun getFromDB(moviesCount: Int): LiveData<List<Movie>>

    fun getFromDB(from: Int, moviesCount: Int): List<Movie>

    fun getSearchedFromDB(query: String): LiveData<List<Movie>>

    fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<Movie>

    fun getSearchedFromDB(query: String, moviesCount: Int): LiveData<List<Movie>>

    fun deleteAllFromDB()

    fun deleteAllFromDBAndPutNew(movies: List<Movie>)
}