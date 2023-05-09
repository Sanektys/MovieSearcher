package com.sandev.moviesearcher.domain

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.MainRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiKey
import com.sandev.moviesearcher.data.themoviedatabase.TmdbResultDto
import com.sandev.moviesearcher.utils.TmdbConverter
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale


class Interactor(private val repo: MainRepository, private val retrofitService: TmdbApi) {

    val favoritesMoviesLiveData = MutableLiveData<List<Movie>>()
    val watchLaterMoviesLiveData = MutableLiveData<List<Movie>>()

    init {
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }

    fun getMoviesFromApi(page: Int, callback: HomeFragmentViewModel.ApiCallback) {
        retrofitService.getMovies(
            apiKey = TmdbApiKey.KEY,
            language = Locale.getDefault().toLanguageTag(),
            page = page)
            .enqueue(object : Callback<TmdbResultDto>{
            override fun onResponse(call: Call<TmdbResultDto>, response: Response<TmdbResultDto>) {
                if (response.isSuccessful) {
                    callback.onSuccess(TmdbConverter.convertApiListToDtoList(response.body()?.results))
                }
            }

            override fun onFailure(call: Call<TmdbResultDto>, t: Throwable) {
                callback.onFailure()
            }
        })
    }

    fun addToFavorite(movie: Movie) {
        repo.favoritesMovies.add(movie)
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
    }

    fun removeFromFavorite(movie: Movie) {
        repo.favoritesMovies.remove(movie)
        favoritesMoviesLiveData.postValue(repo.favoritesMovies.toList())
    }

    fun addToWatchLater(movie: Movie) {
        repo.watchLaterMovies.add(movie)
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }

    fun removeFromWatchLater(movie: Movie) {
        repo.watchLaterMovies.remove(movie)
        watchLaterMoviesLiveData.postValue(repo.watchLaterMovies.toList())
    }
}