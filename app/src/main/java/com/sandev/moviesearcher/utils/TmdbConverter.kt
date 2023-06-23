package com.sandev.moviesearcher.utils

import com.sandev.moviesearcher.data.db.entities.PlayingMovie
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.data.db.entities.TopMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingMovie
import com.sandev.moviesearcher.data.themoviedatabase.TmdbMovieDto


object TmdbConverter {
    fun convertApiDtoListToPlayingMovieList(list: List<TmdbMovieDto>?): List<PlayingMovie> {
        val result = mutableListOf<PlayingMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                PlayingMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToPopularMovieList(list: List<TmdbMovieDto>?): List<PopularMovie> {
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

    fun convertApiDtoListToTopMovieList(list: List<TmdbMovieDto>?): List<TopMovie> {
        val result = mutableListOf<TopMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                TopMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToUpcomingMovieList(list: List<TmdbMovieDto>?): List<UpcomingMovie> {
        val result = mutableListOf<UpcomingMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                UpcomingMovie(
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