package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()
    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    override val interactor: Interactor = App.instance.interactor

    var isLoadingOnProcess: Boolean = false
    var isInSearchMode: Boolean = false
    var latestAttachedMovieCard: Int = 0
    var onFailureFlag: Boolean = false
        set(value) {
            if (value == field) return
            field = value
            if (value) {
                onFailureFlagLiveData.postValue(value)
            }
        }
    private var lastPage: Int = 1

    override var lastSearch: CharSequence?
        set(value) {
            Companion.lastSearch = value
        }
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
            latestAttachedMovieCard = 0
        }
        interactor.getMoviesFromApi(lastPage++, HomeFragmentApiCallback())
    }

    fun getSearchedMoviesFromApi(page: Int = lastPage) {
        if (page != lastPage) {
            lastPage = page
            latestAttachedMovieCard = 0
        }
        //val uriEncodedQuery = URLEncoder.encode(lastSearch.toString(), UTF_8.toString())
        interactor.getSearchedMoviesFromApi(lastSearch.toString(), lastPage++, HomeFragmentApiCallback())
    }


    private inner class HomeFragmentApiCallback : ApiCallback {
        override fun onSuccess(movies: List<Movie>) {
            moviesListLiveData.postValue(movies)
        }

        override fun onFailure() {
            onFailureFlag = true
        }
    }
}