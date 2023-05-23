package com.sandev.moviesearcher.data

import com.sandev.moviesearcher.domain.Movie
import javax.inject.Inject


class MainRepository @Inject constructor() : Repository {
    override val favoritesMovies = mutableListOf<Movie>()
    override val watchLaterMovies = mutableListOf<Movie>()
}