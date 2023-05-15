package com.sandev.moviesearcher.data

import com.sandev.moviesearcher.domain.Movie


class MainRepository {
    val favoritesMovies = mutableListOf<Movie>()
    val watchLaterMovies = mutableListOf<Movie>()
}