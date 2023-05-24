package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.domain.Movie


interface MoviesListRepository {
    val moviesList: MutableList<Movie>
}