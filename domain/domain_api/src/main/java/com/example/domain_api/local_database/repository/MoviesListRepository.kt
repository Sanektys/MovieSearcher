package com.example.domain_api.local_database.repository

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import io.reactivex.rxjava3.core.Observable


interface MoviesListRepository {

    fun putToDB(movies: List<DatabaseMovie>)

    fun getAllFromDB(): Observable<List<DatabaseMovie>>

    fun getFromDB(moviesCount: Int): LiveData<List<DatabaseMovie>>

    fun getFromDB(from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getSearchedFromDB(query: String): LiveData<List<DatabaseMovie>>

    fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getSearchedFromDB(query: String, moviesCount: Int): LiveData<List<DatabaseMovie>>

    fun deleteAllFromDB()

    fun deleteAllFromDBAndPutNew(movies: List<DatabaseMovie>)
}