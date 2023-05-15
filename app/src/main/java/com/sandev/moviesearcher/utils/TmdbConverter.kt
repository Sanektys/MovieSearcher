package com.sandev.moviesearcher.utils

import com.sandev.moviesearcher.data.themoviedatabase.TmdbMovieDto
import com.sandev.moviesearcher.domain.Movie


object TmdbConverter {
    fun convertApiListToDtoList(list: List<TmdbMovieDto>?): List<Movie> {
        val result = mutableListOf<Movie>()
        list?.forEach { tmdbMovie ->
            result.add(Movie(
                poster = tmdbMovie.posterPath,
                title = tmdbMovie.title,
                description = tmdbMovie.overview,
                rating = tmdbMovie.voteAverage
            ))
        }
        return result
    }
}