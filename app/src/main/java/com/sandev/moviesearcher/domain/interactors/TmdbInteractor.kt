package com.sandev.moviesearcher.domain.interactors

import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiKey
import com.sandev.moviesearcher.data.themoviedatabase.TmdbResultDto
import com.sandev.moviesearcher.utils.TmdbConverter
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TmdbInteractor @Inject constructor(private val retrofitService: TmdbApi) {

    private val systemLanguage = Locale.getDefault().toLanguageTag()


    fun getMoviesFromApi(page: Int, callback: MoviesListFragmentViewModel.ApiCallback) {
        retrofitService.getPopularMovies(
            apiKey = TmdbApiKey.KEY,
            language = systemLanguage,
            page = page)
            .enqueue(RetrofitTmdbCallback(callback))
    }

    fun getSearchedMoviesFromApi(query: String, page: Int, callback: MoviesListFragmentViewModel.ApiCallback) {
        retrofitService.getSearchedMovies(
            apiKey = TmdbApiKey.KEY,
            query = query,
            language = systemLanguage,
            page = page)
            .enqueue(RetrofitTmdbCallback(callback))
    }


    private class RetrofitTmdbCallback(val viewModelCallback: MoviesListFragmentViewModel.ApiCallback)
        : Callback<TmdbResultDto> {

        override fun onResponse(call: Call<TmdbResultDto>, response: Response<TmdbResultDto>) {
            if (response.isSuccessful) {
                viewModelCallback.onSuccess(
                    TmdbConverter.convertApiListToDtoList(response.body()?.results),
                response.body()?.totalPages ?: 0)
            }
        }

        override fun onFailure(call: Call<TmdbResultDto>, t: Throwable) {
            viewModelCallback.onFailure()
        }
    }
}