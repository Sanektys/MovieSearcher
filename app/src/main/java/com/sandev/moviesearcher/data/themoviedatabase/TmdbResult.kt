package com.sandev.moviesearcher.data.themoviedatabase

import com.sandev.moviesearcher.data.db.entities.DatabaseMovie


data class TmdbResult(
    val movies: List<DatabaseMovie>,
    val totalPages: Int
)