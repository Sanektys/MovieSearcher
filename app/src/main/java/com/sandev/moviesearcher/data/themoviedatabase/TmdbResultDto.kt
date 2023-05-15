package com.sandev.moviesearcher.data.themoviedatabase

import com.google.gson.annotations.SerializedName


data class TmdbResultDto(
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<TmdbMovieDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)