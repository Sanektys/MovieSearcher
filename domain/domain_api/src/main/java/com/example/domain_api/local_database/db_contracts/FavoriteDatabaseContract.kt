package com.example.domain_api.local_database.db_contracts

import com.example.domain_api.local_database.daos.FavoriteMovieDao


interface FavoriteDatabaseContract {

    fun provideDao(): FavoriteMovieDao
}