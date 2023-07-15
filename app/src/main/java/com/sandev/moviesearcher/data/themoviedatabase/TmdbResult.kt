package com.sandev.moviesearcher.data.themoviedatabase

import com.sandev.moviesearcher.data.db.entities.Movie


data class TmdbResult(
    val movies: List<Movie>,
    val totalPages: Int
)