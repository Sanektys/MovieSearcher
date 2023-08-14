package com.example.domain_api.local_database.db_contracts

import com.example.domain_api.local_database.daos.WatchLaterMovieDao


interface WatchLaterDatabaseContract {

    fun provideDao(): WatchLaterMovieDao
}