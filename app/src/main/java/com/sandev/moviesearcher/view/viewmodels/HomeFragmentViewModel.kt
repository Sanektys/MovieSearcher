package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
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

    private val sharedPreferencesStateListener: SharedPreferences.OnSharedPreferenceChangeListener

    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    private var currentRepositoryType: TmdbInteractor.RepositoryType

    var onFailureFlag: Boolean = false
        private set(value) {
            field = value
            onFailureFlagLiveData.postValue(value)
        }

    override var lastSearch: String
        set(value) {
            Companion.lastSearch = value
        }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) {
            Companion.isInSearchMode = value
        }
        get() = Companion.isInSearchMode

    private val homeFragmentApiCallback = HomeFragmentApiCallback()


    init {
        App.instance.getAppComponent().inject(this)

        currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

        moviesListLiveData.observeForever { newList ->
            moviesPerPage = newList.size
            moviesDatabase = newList.toList()
        }

        sharedPreferencesStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_CATEGORY ->  {
                    currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

                    dispatchQueryToInteractor(query = lastSearch, page = INITIAL_PAGE_IN_RECYCLER)
                }
            }
        }
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesStateListener)

        dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
    }


    override fun onCleared() {
        sharedPreferencesInteractor.removeSharedPreferencesChangeListener(sharedPreferencesStateListener)
    }

    private fun getMoviesFromApi(page: Int) {
        if (page != nextPage) {
            nextPage = page
            lastVisibleMovieCard = 0
        }

        if (isOffline) {
            homeFragmentApiCallback.onFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        Executors.newSingleThreadExecutor().execute {
            interactor.getMoviesFromApi(
                page = nextPage,
                callback = homeFragmentApiCallback,
                repositoryType = provideCurrentMovieListTypeByCategoryInSettings(),
                isNeededWipeBeforePutData = isNeedRefreshLocalDB
            )
            isNeedRefreshLocalDB = false
        }
    }

    private fun getSearchedMoviesFromApi(query: CharSequence, page: Int) {
        isNeedRefreshLocalDB = false

        if (page != nextPage) {
            nextPage = page
            lastVisibleMovieCard = 0
        }

        if (isOffline) {
            homeFragmentApiCallback.onFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        Executors.newSingleThreadExecutor().execute {
            interactor.getSearchedMoviesFromApi(
                query = query.toString(),
                page = nextPage,
                callback = homeFragmentApiCallback
            )
        }
    }

    override fun dispatchQueryToInteractor(query: String?, page: Int?) {
        if (isInSearchMode) {
            if (page != null) {
                getSearchedMoviesFromApi(query = query ?: lastSearch, page = page)
            } else {
                getSearchedMoviesFromApi(query = query ?: lastSearch, page = nextPage)
            }
        } else {
            if (page != null) {
                getMoviesFromApi(page = page)
            } else {
                getMoviesFromApi(page = nextPage)
            }
        }
    }

    private fun loadListFromDB() {
        if (isInSearchMode) {
            Executors.newSingleThreadExecutor().execute {
                moviesListLiveData.postValue(interactor.getSearchedMoviesFromDB(
                    query = lastSearch,
                    page = nextPage,
                    moviesPerPage = moviesPerPage,
                    repositoryType = provideCurrentMovieListTypeByCategoryInSettings()
                ))
            }
        } else {
            Executors.newSingleThreadExecutor().execute {
                moviesListLiveData.postValue(interactor.getMoviesFromDB(
                    page = nextPage,
                    moviesPerPage = moviesPerPage,
                    repositoryType = provideCurrentMovieListTypeByCategoryInSettings()
                ))
            }
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
            onFailureFlag = true
            isOffline = true

            moviesPerPage = TmdbInteractor.INITIAL_MOVIES_COUNT_PER_PAGE

            loadListFromDB()
        }
    }


    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String = ""
    }
}