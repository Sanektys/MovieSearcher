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

    override var lastSearch: CharSequence?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    companion object {
        private var lastSearch: CharSequence? = null
    }

    init {
        interactor.getMoviesFromApi(1, object : ApiCallback {
            override fun onSuccess(movies: List<Movie>) {
                moviesListLiveData.postValue(movies)
            }

            override fun onFailure() {
                onFailureFlagLiveData.postValue(!onFailureFlagLiveData.value!!)
            }
        })
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    interface ApiCallback {
        fun onSuccess(movies: List<Movie>)
        fun onFailure()
    }
}