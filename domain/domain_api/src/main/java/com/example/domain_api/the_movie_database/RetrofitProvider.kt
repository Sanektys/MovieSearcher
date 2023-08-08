package com.example.domain_api.the_movie_database

import com.example.domain_api.the_movie_database.api.TmdbApi


interface RetrofitProvider {
    val tmdbApi: TmdbApi
}