package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.Movie


interface MoviesListRepository {

    fun putToDB(movies: List<Movie>)

    fun getAllFromDB(): LiveData<List<Movie>>

    fun getSearchedFromDB(query: String): LiveData<List<Movie>>

    fun deleteAllFromDB()

    fun deleteAllFromDBAndPutNew(movies: List<Movie>)
}