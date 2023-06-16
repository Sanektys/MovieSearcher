package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import java.util.concurrent.Executors
import javax.inject.Inject


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var interactor: TmdbInteractor

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    var isOffline: Boolean = false
    var isNeedRefreshLocalDB: Boolean = false

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


    override fun searchInDatabase(query: CharSequence): List<Movie>? {
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
        Executors.newSingleThreadExecutor().execute {
            interactor.getMoviesFromApi(
                page = lastPage++,
                callback = homeFragmentApiCallback,
                repositoryType = provideCurrentMovieListTypeByCategoryInSettings(),
                isNeededWipeBeforePutData = isNeedRefreshLocalDB
            )
            isNeedRefreshLocalDB = false
        }
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
        Executors.newSingleThreadExecutor().execute {
            interactor.getSearchedMoviesFromApi(
                query = query.toString(),
                page = lastPage++,
                callback = homeFragmentApiCallback
            )
        }
    }

    private fun provideCurrentMovieListTypeByCategoryInSettings(): TmdbInteractor.RepositoryType {
        return when (sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList()) {
            SharedPreferencesProvider.CATEGORY_POPULAR  -> TmdbInteractor.RepositoryType.POPULAR_MOVIES
            SharedPreferencesProvider.CATEGORY_TOP      -> TmdbInteractor.RepositoryType.TOP_MOVIES
            SharedPreferencesProvider.CATEGORY_UPCOMING -> TmdbInteractor.RepositoryType.UPCOMING_MOVIES
            SharedPreferencesProvider.CATEGORY_PLAYING  -> TmdbInteractor.RepositoryType.PLAYING_MOVIES
            else -> throw java.lang.IllegalStateException("Unknown repository type")
        }
    }


    private inner class HomeFragmentApiCallback : ApiCallback {
        override fun onSuccess(movies: List<Movie>, totalPages: Int) {
            onFailureFlag = false
            totalPagesInLastQuery = totalPages
            moviesListLiveData.postValue(movies)
        }

        override fun onFailure() {
            isOffline = true
            onFailureFlag = true

            if (isInSearchMode) {
                Executors.newSingleThreadExecutor().execute {
                    moviesListLiveData.postValue(
                        interactor.getSearchedMoviesFromDB(
                            query = lastSearch ?: "",
                            repositoryType = provideCurrentMovieListTypeByCategoryInSettings()
                        )
                    )
                }
            } else {
                Executors.newSingleThreadExecutor().execute {
                    moviesListLiveData.postValue(
                        interactor.getMoviesFromDB(provideCurrentMovieListTypeByCategoryInSettings())
                    )
                }
            }
        }
    }
}