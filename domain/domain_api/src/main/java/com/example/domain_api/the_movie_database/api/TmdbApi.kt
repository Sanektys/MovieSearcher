package com.example.domain_api.the_movie_database.api

import com.example.domain_api.the_movie_database.dto.TmdbResultDto
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface TmdbApi {
    @GET("movie/{category}")
    fun getMovies(
        @Path("category") category: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TmdbResultDto>

    @GET("search/movie")
    fun getSearchedMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<TmdbResultDto>

    @GET("search/movie")
    suspend fun getSearchedMoviesByYear(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("primary_release_year") year: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): TmdbResultDto
}