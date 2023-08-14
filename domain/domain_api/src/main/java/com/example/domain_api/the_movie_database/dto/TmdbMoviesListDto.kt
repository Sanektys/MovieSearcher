package com.example.domain_api.the_movie_database.dto

import com.example.domain_api.dto.Movie


data class TmdbMoviesListDto(
    val movies: List<Movie>,
    val totalPages: Int
)