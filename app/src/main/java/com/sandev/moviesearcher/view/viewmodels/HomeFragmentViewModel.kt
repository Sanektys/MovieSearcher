package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.launch


class HomeFragmentViewModel(private val interactor: TmdbInteractor) : MoviesListFragmentViewModel() {

    override val moviesList = MutableLiveData<List<DatabaseMovie>>()

    private val onFailureFlagLiveData = MutableLiveData<Boolean>()
    val getOnFailureFlag: LiveData<Boolean> = onFailureFlagLiveData

    private val isSwipeRefreshActive = MutableLiveData<Boolean>()
    val getSwipeRefreshState: LiveData<Boolean> = isSwipeRefreshActive

    private val sharedPreferencesStateListener: SharedPreferences.OnSharedPreferenceChangeListener

    private var currentRepositoryType: TmdbInteractor.RepositoryType = TmdbInteractor.RepositoryType.POPULAR_MOVIES

    var onFailureFlag: Boolean = false
        private set(value) {
            field = value
            onFailureFlagLiveData.postValue(value)
            isSwipeRefreshActive.postValue(false)
        }

    override val recyclerAdapter: MoviesRecyclerAdapter =
        MoviesRecyclerAdapter(sharedPreferencesInteractor.isRatingDonutAnimationEnabled())

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
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(recyclerAdapter.sharedPreferencesCallback)

        moviesList.observeForever { newList ->
            moviesPerPage = newList.size
            moviesDatabase = newList.toList()
        }

        sharedPreferencesStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_CATEGORY, SharedPreferencesProvider.KEY_LANGUAGE ->
                    viewModelScope.launch {
                        isSwipeRefreshActive.postValue(true)
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
        ).subscribe(@Suppress("UNCHECKED_CAST")
            onSuccess@ { result ->
                totalPagesInLastQuery = result.totalPages
                val movies = result.movies as List<DatabaseMovie>

                var queryToLocalDB: Disposable? = null
                queryToLocalDB = if (isNeedRefreshLocalDB) {
                    interactor.deleteAllMoviesFromDbAndPutNewMovies(movies, repositoryTypeOnQuery)
                        .subscribe {
                            queryToLocalDB?.dispose()
                        }
                } else {
                    interactor.putMoviesToDB(movies, repositoryTypeOnQuery).subscribe {
                        queryToLocalDB?.dispose()
                    }
                }
                isNeedRefreshLocalDB = false

                onQuerySuccess(movies)

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
        ).subscribe(@Suppress("UNCHECKED_CAST")
            onSuccess@ { result ->
                totalPagesInLastQuery = result.totalPages

                onQuerySuccess(result.movies as List<DatabaseMovie>)

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


    class ViewModelFactory(private val interactor: TmdbInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                return HomeFragmentViewModel(interactor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    companion object {
        private var isInSearchMode: Boolean = false
        private var lastSearch: String = ""
    }
}