package com.sandev.moviesearcher.data

import com.sandev.moviesearcher.domain.Movie


interface Repository {
    val favoritesMovies: MutableList<Movie>
    val watchLaterMovies: MutableList<Movie>
}