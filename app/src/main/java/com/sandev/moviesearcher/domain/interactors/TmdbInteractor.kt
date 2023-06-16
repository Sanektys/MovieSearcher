package com.sandev.moviesearcher.domain.interactors

import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.PlayingMoviesListRepository
import com.sandev.moviesearcher.data.repositories.PopularMoviesListRepository
import com.sandev.moviesearcher.data.repositories.TopMoviesListRepository
import com.sandev.moviesearcher.data.repositories.UpcomingMoviesListRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiKey
import com.sandev.moviesearcher.data.themoviedatabase.TmdbResultDto
import com.sandev.moviesearcher.utils.TmdbConverter
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Singleton


@Singleton
class TmdbInteractor(private val retrofitService: TmdbApi,
                     private val sharedPreferences: SharedPreferencesProvider,
                     private vararg val moviesListRepositories: MoviesListRepository
) {

    private val systemLanguage = Locale.getDefault().toLanguageTag()

    private var popularMoviesRepositoryIndex: Int = 0
    private var topMoviesRepositoryIndex: Int = 0
    private var upcomingMoviesRepositoryIndex: Int = 0
    private var playingMoviesRepositoryIndex: Int = 0

    init {
        for (i in moviesListRepositories.indices) {
            when (moviesListRepositories[i]) {
                is PopularMoviesListRepository -> popularMoviesRepositoryIndex = i
                is TopMoviesListRepository -> topMoviesRepositoryIndex = i
                is UpcomingMoviesListRepository -> upcomingMoviesRepositoryIndex = i
                is PlayingMoviesListRepository -> playingMoviesRepositoryIndex = i
            }
        }
    }


    fun getMoviesFromApi(page: Int, callback: MoviesListFragmentViewModel.ApiCallback, repositoryType: RepositoryType) {
        retrofitService.getMovies(
            apiKey = TmdbApiKey.KEY,
            category = sharedPreferences.getDefaultCategory(),
            language = systemLanguage,
            page = page
        ).enqueue(RetrofitTmdbCallback(callback, getRequestedRepository(repositoryType)))
    }

    fun getSearchedMoviesFromApi(query: String, page: Int, callback: MoviesListFragmentViewModel.ApiCallback) {
        retrofitService.getSearchedMovies(
            apiKey = TmdbApiKey.KEY,
            query = query,
            language = systemLanguage,
            page = page
        ).enqueue(RetrofitTmdbCallback(callback))
    }

    fun getMoviesFromDB(repositoryType: RepositoryType) = getRequestedRepository(repositoryType).getAllFromDB()

    fun getSearchedMoviesFromDB(query: String, repositoryType: RepositoryType)
            = getRequestedRepository(repositoryType).getSearchedFromDB(query)

    fun deleteAllCachedMoviesFromDB(repositoryType: RepositoryType)
            = getRequestedRepository(repositoryType).deleteAllFromDB()


    private fun getRequestedRepository(repositoryType: RepositoryType): MoviesListRepository {
        return when (repositoryType) {
            RepositoryType.POPULAR_MOVIES  -> moviesListRepositories[popularMoviesRepositoryIndex]
            RepositoryType.TOP_MOVIES      -> moviesListRepositories[topMoviesRepositoryIndex]
            RepositoryType.UPCOMING_MOVIES -> moviesListRepositories[upcomingMoviesRepositoryIndex]
            RepositoryType.PLAYING_MOVIES  -> moviesListRepositories[playingMoviesRepositoryIndex]
        }
    }


    private class RetrofitTmdbCallback(
        private val viewModelCallback: MoviesListFragmentViewModel.ApiCallback,
        private val moviesListRepository: MoviesListRepository? = null
    ) : Callback<TmdbResultDto> {

        override fun onResponse(call: Call<TmdbResultDto>, response: Response<TmdbResultDto>) {
            if (response.isSuccessful) {
                val moviesList = TmdbConverter.convertApiListToDtoList(response.body()?.results)

                moviesListRepository?.putToDB(moviesList)

                viewModelCallback.onSuccess(moviesList, response.body()?.totalPages ?: 0)
            }
        }

        override fun onFailure(call: Call<TmdbResultDto>, t: Throwable) {
            viewModelCallback.onFailure()
        }
    }


    enum class RepositoryType {
        POPULAR_MOVIES,
        TOP_MOVIES,
        UPCOMING_MOVIES,
        PLAYING_MOVIES
    }
}