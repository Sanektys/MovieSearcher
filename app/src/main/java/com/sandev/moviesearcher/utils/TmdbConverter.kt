package com.sandev.moviesearcher.utils

import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.data.themoviedatabase.TmdbMovieDto


object TmdbConverter {
    fun convertApiListToDtoList(list: List<TmdbMovieDto>?): List<Movie> {
        val result = mutableListOf<Movie>()
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