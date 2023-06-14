package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.entities.PopularMovie


interface MoviesListRepository {

    fun putToDB(movie: PopularMovie): Long

    fun getAllFromDB(): List<PopularMovie>

    fun getSearchedFromDB(query: String): List<PopularMovie>
}