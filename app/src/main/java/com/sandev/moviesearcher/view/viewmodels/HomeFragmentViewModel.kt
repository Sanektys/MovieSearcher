package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var interactor: TmdbInteractor

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    override val moviesList = MutableLiveData<List<Movie>>()

    private val onFailureFlagLiveData = MutableLiveData<Boolean>()
    val getOnFailureFlag: LiveData<Boolean> = onFailureFlagLiveData

    private val sharedPreferencesStateListener: SharedPreferences.OnSharedPreferenceChangeListener

    private var currentRepositoryType: TmdbInteractor.RepositoryType = TmdbInteractor.RepositoryType.POPULAR_MOVIES

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


    init {
        App.instance.getAppComponent().inject(this)

        moviesList.observeForever { newList ->
            moviesPerPage = newList.size
            moviesDatabase = newList.toList()
        }

        sharedPreferencesStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_CATEGORY ->  viewModelScope.launch {
                    currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

                    dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
                }
            }
        }
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesStateListener)

        viewModelScope.launch {
            currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

            dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
        }
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
            onQueryFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        viewModelScope.launch {
            val repositoryTypeOnQuery = currentRepositoryType
            var resultMovies: List<Movie> = listOf()

            try {
                interactor.getMoviesFromApi(
                    page = nextPage,
                    repositoryType = repositoryTypeOnQuery
                ).collect { result ->
                    resultMovies = result.movies
                    totalPagesInLastQuery = result.totalPages
                }
            } catch (e: Exception) {
                onQueryFailure()
                isNeedRefreshLocalDB = false
                return@launch
            }

            if (isNeedRefreshLocalDB) {
                interactor.deleteAllMoviesFromDbAndPutNewMovies(resultMovies, repositoryTypeOnQuery)
            } else {
                interactor.putMoviesToDB(resultMovies, repositoryTypeOnQuery)
            }
            isNeedRefreshLocalDB = false

            onQuerySuccess(resultMovies)
        }
    }

    private fun getSearchedMoviesFromApi(page: Int) {
        isNeedRefreshLocalDB = false

        if (page != nextPage) {
            nextPage = page
            lastVisibleMovieCard = 0
        }

        if (isOffline) {
            onQueryFailure()
            return
        }

        if (page > totalPagesInLastQuery) return

        viewModelScope.launch {
            var resultMovies: List<Movie> = listOf()

            try {
                interactor.getSearchedMoviesFromApi(
                    query = lastSearch,
                    page = nextPage
                ).collect { result ->
                    resultMovies = result.movies
                    totalPagesInLastQuery = result.totalPages
                }
            } catch (e: Exception) {
                onQueryFailure()
                return@launch
            }
            onQuerySuccess(resultMovies)
        }
    }

    override fun dispatchQueryToInteractor(page: Int?) {
        if (isInSearchMode) {
            if (page != null) {
                getSearchedMoviesFromApi(page = page)
            } else {
                getSearchedMoviesFromApi(page = nextPage)
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
            viewModelScope.launch {
                moviesList.postValue(interactor.getSearchedMoviesFromDB(
                    query = lastSearch,
                    page = nextPage,
                    moviesPerPage = moviesPerPage,
                    repositoryType = currentRepositoryType
                ))
            }
        } else {
            viewModelScope.launch {
                moviesList.postValue(interactor.getMoviesFromDB(
                    page = nextPage,
                    moviesPerPage = moviesPerPage,
                    repositoryType = currentRepositoryType
                ))
            }
        }
    }

    private suspend fun provideCurrentMovieListTypeByCategoryInSettings(): TmdbInteractor.RepositoryType {
        return when (sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList()) {
            SharedPreferencesProvider.CATEGORY_POPULAR  -> TmdbInteractor.RepositoryType.POPULAR_MOVIES
            SharedPreferencesProvider.CATEGORY_TOP      -> TmdbInteractor.RepositoryType.TOP_MOVIES
            SharedPreferencesProvider.CATEGORY_UPCOMING -> TmdbInteractor.RepositoryType.UPCOMING_MOVIES
            SharedPreferencesProvider.CATEGORY_PLAYING  -> TmdbInteractor.RepositoryType.PLAYING_MOVIES
            else -> throw java.lang.IllegalStateException("Unknown repository type")
        }
    }

    private fun onQuerySuccess(movies: List<Movie>) {
        onFailureFlag = false

        moviesList.postValue(movies)
    }

    private fun onQueryFailure() {
        onFailureFlag = true
        isOffline = true

        moviesPerPage = TmdbInteractor.INITIAL_MOVIES_COUNT_PER_PAGE

        loadListFromDB()
    }


    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String = ""
    }
}