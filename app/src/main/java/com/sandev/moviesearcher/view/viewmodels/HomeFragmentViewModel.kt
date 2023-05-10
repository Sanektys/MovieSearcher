package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()
    val onFailureFlagLiveData = MutableLiveData(false)

    override val interactor: Interactor = App.instance.interactor

    var onFailureFlag: Boolean = false
    var isLoadingOnProcess: Boolean = false
    var latestShowedMovieCard: Int = 0
    private var lastPage: Int = 1

    override var lastSearch: CharSequence?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    companion object {
        private var lastSearch: CharSequence? = null
    }

    init {
        getMoviesFromApi()
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun getMoviesFromApi(page: Int = lastPage) {
        if (page != lastPage) {
            lastPage = page
            latestShowedMovieCard = 0
        }
        interactor.getMoviesFromApi(lastPage++, object : ApiCallback {
            override fun onSuccess(movies: List<Movie>) {
                moviesListLiveData.postValue(movies)
            }

            override fun onFailure() {
                onFailureFlagLiveData.postValue(!onFailureFlagLiveData.value!!)
            }
        })
    }

    interface ApiCallback {
        fun onSuccess(movies: List<Movie>)
        fun onFailure()
    }
}