package com.sandev.moviesearcher.domain.interactors

import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
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
class TmdbInteractor @Inject constructor(private val retrofitService: TmdbApi,
                                         private val sharedPreferences: SharedPreferencesProvider,
                                         private val moviesListRepository: MoviesListRepository
) {

    private val systemLanguage = Locale.getDefault().toLanguageTag()


    fun getMoviesFromApi(page: Int, callback: MoviesListFragmentViewModel.ApiCallback) {
        retrofitService.getMovies(
            apiKey = TmdbApiKey.KEY,
            category = sharedPreferences.getDefaultCategory(),
            language = systemLanguage,
            page = page
        ).enqueue(RetrofitTmdbCallback(callback, moviesListRepository))
    }

    fun getSearchedMoviesFromApi(query: String, page: Int, callback: MoviesListFragmentViewModel.ApiCallback) {
        retrofitService.getSearchedMovies(
            apiKey = TmdbApiKey.KEY,
            query = query,
            language = systemLanguage,
            page = page
        ).enqueue(RetrofitTmdbCallback(callback))
    }

    fun getMoviesFromDB() = moviesListRepository.getAllFromDB()

    fun getSearchedMoviesFromDB(query: String) = moviesListRepository.getSearchedFromDB(query)


    private class RetrofitTmdbCallback(
        private val viewModelCallback: MoviesListFragmentViewModel.ApiCallback,
        private val moviesListRepository: MoviesListRepository? = null
    ) : Callback<TmdbResultDto> {

        override fun onResponse(call: Call<TmdbResultDto>, response: Response<TmdbResultDto>) {
            if (response.isSuccessful) {
                val moviesList = TmdbConverter.convertApiListToDtoList(response.body()?.results)

                if (moviesListRepository != null) {
                    moviesList.forEach { movie ->
                        moviesListRepository.putToDB(movie)
                    }
                }

                viewModelCallback.onSuccess(moviesList, response.body()?.totalPages ?: 0)
            }
        }

        override fun onFailure(call: Call<TmdbResultDto>, t: Throwable) {
            viewModelCallback.onFailure()
        }
    }
}