package com.example.domain_api.the_movie_database.dto


data class TmdbMoviesListDto(
    val movies: List<MovieDto>,
    val totalPages: Int
)