package com.example.domain_api.local_database.daos

import com.example.domain_api.local_database.entities.TitleAndDescription


interface SavedMovieDao : MovieDao {

    fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int
}