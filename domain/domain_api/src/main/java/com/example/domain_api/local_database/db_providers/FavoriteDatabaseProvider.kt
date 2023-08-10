package com.example.domain_api.local_database.db_providers

import com.example.domain_api.local_database.daos.FavoriteMovieDao


interface FavoriteDatabaseProvider {

    fun provideDao(): FavoriteMovieDao
}