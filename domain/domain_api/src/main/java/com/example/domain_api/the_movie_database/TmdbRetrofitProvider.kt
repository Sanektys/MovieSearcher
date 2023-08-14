package com.example.domain_api.the_movie_database

import com.example.domain_api.the_movie_database.api.TmdbApi


interface TmdbRetrofitProvider {
    val tmdbApi: TmdbApi
}