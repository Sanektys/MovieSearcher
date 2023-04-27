package com.sandev.moviesearcher.domain

import com.sandev.moviesearcher.data.MainRepository


class Interactor(private val repo: MainRepository) {
    fun getMoviesDB(): List<Movie> = repo.moviesDatabase
}