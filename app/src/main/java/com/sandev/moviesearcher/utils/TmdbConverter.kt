package com.sandev.moviesearcher.utils

import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.db.entities.PlayingDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.PopularDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.TopDatabaseMovie
import com.sandev.moviesearcher.data.db.entities.UpcomingDatabaseMovie
import com.sandev.moviesearcher.data.themoviedatabase.TmdbMovieDto


object TmdbConverter {
    fun convertApiDtoListToPlayingMovieList(list: List<TmdbMovieDto>?): List<PlayingDatabaseMovie> {
        val result = mutableListOf<PlayingDatabaseMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                PlayingDatabaseMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToPopularMovieList(list: List<TmdbMovieDto>?): List<PopularDatabaseMovie> {
        val result = mutableListOf<PopularDatabaseMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                PopularDatabaseMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToTopMovieList(list: List<TmdbMovieDto>?): List<TopDatabaseMovie> {
        val result = mutableListOf<TopDatabaseMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                TopDatabaseMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToUpcomingMovieList(list: List<TmdbMovieDto>?): List<UpcomingDatabaseMovie> {
        val result = mutableListOf<UpcomingDatabaseMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                UpcomingDatabaseMovie(
                    poster = tmdbMovie.posterPath,
                    title = tmdbMovie.title,
                    description = tmdbMovie.overview,
                    rating = tmdbMovie.voteAverage
                )
            )
        }
        return result
    }

    fun convertApiDtoListToMovieList(list: List<TmdbMovieDto>?): List<DatabaseMovie> {
        val result = mutableListOf<DatabaseMovie>()
        list?.forEach { tmdbMovie ->
            result.add(
                UpcomingDatabaseMovie(
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