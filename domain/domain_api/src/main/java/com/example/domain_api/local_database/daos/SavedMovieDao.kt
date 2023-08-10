package com.example.domain_api.local_database.daos

import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


interface SavedMovieDao : MovieDao {

    fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int
}