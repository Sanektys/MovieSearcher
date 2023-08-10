package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var interactor: TmdbInteractor

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    override val moviesList = MutableLiveData<List<DatabaseMovie>>()

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


    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()

    fun fullRefreshMoviesList() {
        isOffline = false
        isNeedRefreshLocalDB = true
        lastVisibleMovieCard = 0
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
            onQueryFailure()
            return
        }

        if (page > totalPagesInLastQuery) return


        val repositoryTypeOnQuery = currentRepositoryType
        val moviesCategory = when (repositoryTypeOnQuery) {
            TmdbInteractor.RepositoryType.TOP_MOVIES      -> SharedPreferencesProvider.CATEGORY_TOP
            TmdbInteractor.RepositoryType.POPULAR_MOVIES  -> SharedPreferencesProvider.CATEGORY_POPULAR
            TmdbInteractor.RepositoryType.UPCOMING_MOVIES -> SharedPreferencesProvider.CATEGORY_UPCOMING
            TmdbInteractor.RepositoryType.PLAYING_MOVIES  -> SharedPreferencesProvider.CATEGORY_PLAYING
        }

        var queryToApi: Disposable? = null
        queryToApi = interactor.getMoviesFromApi(
            page = nextPage,
            moviesCategory = moviesCategory,
            repositoryType = repositoryTypeOnQuery
        ).subscribe(
            onSuccess@ { result ->
                totalPagesInLastQuery = result.totalPages

                var queryToLocalDB: Disposable? = null
                queryToLocalDB = if (isNeedRefreshLocalDB) {
                    interactor.deleteAllMoviesFromDbAndPutNewMovies(result.movies, repositoryTypeOnQuery)
                        .subscribe {
                            queryToLocalDB?.dispose()
                        }
                } else {
                    interactor.putMoviesToDB(result.movies, repositoryTypeOnQuery).subscribe {
                        queryToLocalDB?.dispose()
                    }
                }
                isNeedRefreshLocalDB = false

                onQuerySuccess(result.movies)

                queryToApi?.dispose()
            },
            onError@ {
                onQueryFailure()
                isNeedRefreshLocalDB = false

                queryToApi?.dispose()
            }
        )
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


        var queryToApi: Disposable? = null
        queryToApi = interactor.getSearchedMoviesFromApi(
            query = lastSearch,
            page = nextPage
        ).subscribe(
            onSuccess@ { result ->
                totalPagesInLastQuery = result.totalPages

                onQuerySuccess(result.movies)

                queryToApi?.dispose()
            },
            onError@ {
                onQueryFailure()

                queryToApi?.dispose()
            }
        )
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
        var disposable: Disposable? = null

        disposable = if (isInSearchMode) {
            interactor.getSearchedMoviesFromDB(
                query = lastSearch,
                page = nextPage,
                moviesPerPage = moviesPerPage,
                repositoryType = currentRepositoryType
            ).subscribe { movies ->
                moviesList.value = movies
                disposable?.dispose()
            }
        } else {
            interactor.getMoviesFromDB(
                page = nextPage,
                moviesPerPage = moviesPerPage,
                repositoryType = currentRepositoryType
            ).subscribe { movies ->
                moviesList.value = movies
                disposable?.dispose()
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

    private fun onQuerySuccess(movies: List<DatabaseMovie>) {
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