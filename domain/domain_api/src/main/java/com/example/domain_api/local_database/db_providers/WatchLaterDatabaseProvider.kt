package com.example.domain_api.local_database.db_providers

import com.example.domain_api.local_database.daos.WatchLaterMovieDao


interface WatchLaterDatabaseProvider {

    fun provideDao(): WatchLaterMovieDao
}