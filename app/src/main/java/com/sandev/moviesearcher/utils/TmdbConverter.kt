package com.sandev.moviesearcher.utils

import com.sandev.moviesearcher.data.themoviedatabase.TmdbMovieDto
import com.sandev.moviesearcher.data.db.entities.PopularMovie


object TmdbConverter {
    fun convertApiListToDtoList(list: List<TmdbMovieDto>?): List<PopularMovie> {
        val result = mutableListOf<PopularMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                PopularMovie(
                poster = tmdbMovie.posterPath,
                title = tmdbMovie.title,
                description = tmdbMovie.overview,
                rating = tmdbMovie.voteAverage
            )
            )
        }
        return result
    }
}