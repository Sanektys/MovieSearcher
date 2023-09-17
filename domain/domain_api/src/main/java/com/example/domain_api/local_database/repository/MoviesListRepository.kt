package com.example.domain_api.local_database.repository

import com.example.domain_api.local_database.entities.DatabaseMovie
import io.reactivex.rxjava3.core.Observable


interface MoviesListRepository {

    fun putToDB(movies: List<DatabaseMovie>)

    fun getAllFromDB(): Observable<List<DatabaseMovie>>

    fun getFromDB(from: Int, moviesCount: Int): List<DatabaseMovie>

    fun getSearchedFromDB(query: String, from: Int, moviesCount: Int): List<DatabaseMovie>

    fun deleteAllFromDB()

    fun deleteAllFromDBAndPutNew(movies: List<DatabaseMovie>)
}