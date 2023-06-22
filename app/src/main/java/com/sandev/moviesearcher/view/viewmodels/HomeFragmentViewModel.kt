package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

    override val moviesListLiveData = MediatorLiveData<List<Movie>>()

    private var listFromDbLiveData: LiveData<List<Movie>>? = null
    private var searchedListFromDbLiveData: LiveData<List<Movie>>? = null

    private val sharedPreferencesStateListener: SharedPreferences.OnSharedPreferenceChangeListener

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


    init {
        App.instance.getAppComponent().inject(this)

        if (isInSearchMode) {
            getSearchedMoviesFromApi()
        } else {
            getMoviesFromApi()
        }

        initializeDatabaseMoviesListsLiveData()
        addSourcesToGeneralMoviesListLiveData()

        sharedPreferencesStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_CATEGORY ->  {
                    removeSourcesFromGeneralMoviesListLiveData()
                    initializeDatabaseMoviesListsLiveData()
                    addSourcesToGeneralMoviesListLiveData()

                    refreshMoviesList()
                }
            }
        }
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesStateListener)
    }


    override fun onCleared() {
        removeSourcesFromGeneralMoviesListLiveData()
        sharedPreferencesInteractor.removeSharedPreferencesChangeListener(sharedPreferencesStateListener)
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    fun refreshMoviesList() {
        wipeListInMoviesListLiveData()
        if (isInSearchMode) {
            getSearchedMoviesFromApi(page = INITIAL_PAGE_IN_RECYCLER)
        } else {
            getMoviesFromApi(page = INITIAL_PAGE_IN_RECYCLER)
        }
    }

    fun fullRefreshMoviesList() {
        isOffline = false
        isNeedRefreshLocalDB = true
        refreshMoviesList()
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

    private fun wipeListInMoviesListLiveData() {
        moviesListLiveData.value = listOf()
    }

    private fun addSourcesToGeneralMoviesListLiveData() {
        moviesListLiveData.addSource(listFromDbLiveData!!) { moviesList ->
            if (!isInSearchMode) {
                moviesListLiveData.postValue(moviesList)
            }
        }
        addSourceForSearchToGeneralMoviesListLiveData()
    }

    private fun addSourceForSearchToGeneralMoviesListLiveData() {
        moviesListLiveData.addSource(searchedListFromDbLiveData!!) { searchedMoviesList ->
            if (isInSearchMode) {
                moviesListLiveData.postValue(searchedMoviesList)
            }
        }
    }

    private fun updateSourceForSearchInGeneralMoviesListLiveData() {
        moviesListLiveData.removeSource(searchedListFromDbLiveData!!)
        addSourceForSearchToGeneralMoviesListLiveData()
    }

    private fun removeSourcesFromGeneralMoviesListLiveData() {
        moviesListLiveData.removeSource(listFromDbLiveData!!)
        moviesListLiveData.removeSource(searchedListFromDbLiveData!!)
    }

    private fun initializeDatabaseMoviesListsLiveData() {
        listFromDbLiveData = interactor.getMoviesFromDB(
            provideCurrentMovieListTypeByCategoryInSettings()
        )
        initializeDatabaseSearchedMoviesListLiveData(lastSearch ?: "")
    }

    private fun initializeDatabaseSearchedMoviesListLiveData(query: String) {
        searchedListFromDbLiveData = interactor.getSearchedMoviesFromDB(
            query,
            provideCurrentMovieListTypeByCategoryInSettings()
        )
    }


    private inner class HomeFragmentApiCallback : ApiCallback {
        override fun onSuccess(totalPages: Int) {
            onFailureFlag = false
            totalPagesInLastQuery = totalPages
        }

        override fun onFailure() {
            if (onFailureFlag) {
                if (isInSearchMode) {
                    initializeDatabaseSearchedMoviesListLiveData(lastSearch!!)
                    updateSourceForSearchInGeneralMoviesListLiveData()
                } else {
                    moviesListLiveData.postValue(listFromDbLiveData?.value ?: listOf())
                }
            }

            isOffline = true
            onFailureFlag = true
        }
    }


    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String? = null

        private const val INITIAL_PAGE_IN_RECYCLER = 1
    }
}