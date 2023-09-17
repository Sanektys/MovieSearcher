package com.example.domain_api.local_database.repository

import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import io.reactivex.rxjava3.core.Observable


interface WatchLaterListRepository: MoviesListRepositoryForSavedLists {

    fun getAllWatchMoviesLaterFromDB(): Observable<List<WatchLaterDatabaseMovie>>

    fun getWatchLaterMoviesFromDB(from: Int, moviesCount: Int): List<WatchLaterDatabaseMovie>

    fun getSearchedWatchLaterMovieFromDB(query: String, from: Int, moviesCount: Int): List<WatchLaterDatabaseMovie>
}