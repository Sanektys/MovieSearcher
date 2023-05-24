package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.domain.Movie
import javax.inject.Inject


class WatchLaterMoviesRepository @Inject constructor() : MoviesListRepository {
    override val moviesList = mutableListOf<Movie>()
}