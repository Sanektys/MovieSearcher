package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import javax.inject.Inject


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var interactor: TmdbInteractor

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    override val moviesListLiveData = MutableLiveData<List<PopularMovie>>()

    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    var isOffline: Boolean = false

    var isPaginationLoadingOnProcess: Boolean = false
    var latestAttachedMovieCard: Int = 0
    private var lastPage: Int = 1
    private var totalPagesInLastQuery = 1

    var onFailureFlag: Boolean = false
        set(value) {
            field = value
            onFailureFlagLiveData.postValue(value)
        }

    override var lastSearch: String?
        set(value) {
            Companion.lastSearch = value
        }
        get() = Companion.lastSearch
    var isInSearchMode: Boolean
        set(value) {
            Companion.isInSearchMode = value
        }
        get() = Companion.isInSearchMode

    private val homeFragmentApiCallback = HomeFragmentApiCallback()

    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String? = null
    }

    init {
        App.instance.getAppComponent().inject(this)

        if (isInSearchMode) {
            getSearchedMoviesFromApi()
        } else {
            getMoviesFromApi()
        }
    }


    override fun searchInDatabase(query: CharSequence): List<PopularMovie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun isNextPageCanBeDownloaded() = lastPage <= totalPagesInLastQuery

    fun getMoviesFromApi(page: Int = lastPage) {
        if (isOffline) {
            homeFragmentApiCallback.onFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        if (page != lastPage) {
            lastPage = page
            latestAttachedMovieCard = 0
        }
        interactor.getMoviesFromApi(lastPage++, homeFragmentApiCallback)
    }

    fun getSearchedMoviesFromApi(query: CharSequence = lastSearch ?: "", page: Int = lastPage) {
        if (isOffline) {
            homeFragmentApiCallback.onFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        if (page != lastPage) {
            lastPage = page
            latestAttachedMovieCard = 0
        }
        interactor.getSearchedMoviesFromApi(query.toString(), lastPage++, homeFragmentApiCallback)
    }


    private inner class HomeFragmentApiCallback : ApiCallback {
        override fun onSuccess(movies: List<PopularMovie>, totalPages: Int) {
            onFailureFlag = false
            totalPagesInLastQuery = totalPages
            moviesListLiveData.postValue(movies)
        }

        override fun onFailure() {
            isOffline = true
            onFailureFlag = true

            if (isInSearchMode) {
                moviesListLiveData.postValue(interactor.getSearchedMoviesFromDB(lastSearch ?: ""))
            } else {
                moviesListLiveData.postValue(interactor.getMoviesFromDB())
            }
        }
    }
}