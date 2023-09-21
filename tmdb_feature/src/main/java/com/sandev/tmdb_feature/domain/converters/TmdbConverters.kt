package com.sandev.tmdb_feature.domain.converters

import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.PlayingDatabaseMovie
import com.example.domain_api.local_database.entities.PopularDatabaseMovie
import com.example.domain_api.local_database.entities.TopDatabaseMovie
import com.example.domain_api.local_database.entities.UpcomingDatabaseMovie
import com.example.domain_api.the_movie_database.dto.TmdbMovieDto


internal fun convertApiDtoListToPlayingMovieList(list: List<TmdbMovieDto>?): List<PlayingDatabaseMovie> {
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

internal fun convertApiDtoListToPopularMovieList(list: List<TmdbMovieDto>?): List<PopularDatabaseMovie> {
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

internal fun convertApiDtoListToTopMovieList(list: List<TmdbMovieDto>?): List<TopDatabaseMovie> {
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

internal fun convertApiDtoListToUpcomingMovieList(list: List<TmdbMovieDto>?): List<UpcomingDatabaseMovie> {
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

internal fun convertApiDtoListToMovieList(list: List<TmdbMovieDto>?): List<DatabaseMovie> {
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
