package com.sandev.moviesearcher.data.themoviedatabase

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface TmdbApi {
    @GET("movie/{category}")
    suspend fun getMovies(
        @Path("category") category: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): TmdbResultDto

    @GET("search/movie")
    suspend fun getSearchedMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String,
        @Query("page") page: Int
    ): TmdbResultDto
}