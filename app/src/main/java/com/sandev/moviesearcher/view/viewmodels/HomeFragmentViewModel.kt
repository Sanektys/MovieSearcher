package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeFragmentViewModel @Inject constructor(interactor: Interactor): MoviesListFragmentViewModel(interactor) {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()
    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    var isInSearchMode: Boolean
        set(value) {
            Companion.isInSearchMode = value
        }
        get() = Companion.isInSearchMode
    var isPaginationLoadingOnProcess: Boolean = false
    var latestAttachedMovieCard: Int = 0
    var onFailureFlag: Boolean = false
        set(value) {
            field = value
            onFailureFlagLiveData.postValue(value)
        }
    private var lastPage: Int = 1
    private var totalPagesInLastQuery = 1
    override var lastSearch: String?
        set(value) {
            Companion.lastSearch = value
        }
        get() = Companion.lastSearch

    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String? = null
    }

    init {
        if (isInSearchMode) {
            getSearchedMoviesFromApi()
        } else {
            getMoviesFromApi()
        }
    }


    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun isNextPageCanBeDownloaded() = lastPage <= totalPagesInLastQuery

    fun getMoviesFromApi(page: Int = lastPage) {
        if (page > totalPagesInLastQuery) return

        if (page != lastPage) {
            lastPage = page
            latestAttachedMovieCard = 0
        }
        interactor.getMoviesFromApi(lastPage++, HomeFragmentApiCallback())
    }

    fun getSearchedMoviesFromApi(query: CharSequence = lastSearch ?: "", page: Int = lastPage) {
        if (page > totalPagesInLastQuery) return

        if (page != lastPage) {
            lastPage = page
            latestAttachedMovieCard = 0
        }
        interactor.getSearchedMoviesFromApi(query.toString(), lastPage++, HomeFragmentApiCallback())
    }


    private inner class HomeFragmentApiCallback : ApiCallback {
        override fun onSuccess(movies: List<Movie>, totalPages: Int) {
            onFailureFlag = false
            totalPagesInLastQuery = totalPages
            moviesListLiveData.postValue(movies)
        }

        override fun onFailure() {
            onFailureFlag = true
        }
    }
}