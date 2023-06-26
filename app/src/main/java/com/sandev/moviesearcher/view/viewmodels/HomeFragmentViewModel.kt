package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.math.roundToInt


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var interactor: TmdbInteractor

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    private val sharedPreferencesStateListener: SharedPreferences.OnSharedPreferenceChangeListener

    val onFailureFlagLiveData = MutableLiveData<Boolean>()

    var isOffline: Boolean = false
        private set
    private var isNeedRefreshLocalDB: Boolean = false

    private var isPaginationLoadingOnProcess: Boolean = false
    var lastVisibleMovieCard: Int = 0
        private set
    private var nextPage: Int = 1
    private var moviesPerPage: Int = 0
    private var totalPagesInLastQuery = 1

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
    private var isInSearchMode: Boolean
        set(value) {
            Companion.isInSearchMode = value
        }
        get() = Companion.isInSearchMode

    private val homeFragmentApiCallback = HomeFragmentApiCallback()


    init {
        App.instance.getAppComponent().inject(this)

        currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

        moviesListLiveData.observeForever { newList ->
            nextPage
            moviesPerPage = newList.size
            moviesDatabase = newList.toList()
        }

        sharedPreferencesStateListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_CATEGORY ->  {
                    currentRepositoryType = provideCurrentMovieListTypeByCategoryInSettings()

                    dispatchQueryToApi(INITIAL_PAGE_IN_RECYCLER)
                }
            }
        }
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesStateListener)

        dispatchQueryToApi(INITIAL_PAGE_IN_RECYCLER)
    }


    override fun onCleared() {
        sharedPreferencesInteractor.removeSharedPreferencesChangeListener(sharedPreferencesStateListener)
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }

    override fun searchInSearchView(query: String) {
        if (query == lastSearch) return

        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            if (!isInSearchMode) {
                isInSearchMode = true
                recyclerAdapter.clearList()
            }
            getSearchedMoviesFromApi(query, INITIAL_PAGE_IN_RECYCLER)
        } else if (query.isEmpty()) {
            if (isInSearchMode) {
                isInSearchMode = false
                recyclerAdapter.clearList()
            }
            getMoviesFromApi(INITIAL_PAGE_IN_RECYCLER)
        }
        lastSearch = query
    }

    override fun initializeRecyclerAdapterList() {
        if (isInSearchMode) {
            if (isPaginationLoadingOnProcess) {
                // Если строка поиска не пуста (isInSearchMode = true) и происходит подгрузка
                // при скролле - добавлять новые списки в конец
                recyclerAdapter.addMovieCards(moviesDatabase)
            } else {
                // Если в поле поиска был произведён ввод, то устанавливается новый список
                recyclerAdapter.setList(moviesDatabase)
            }
        } else {
            if (!isPaginationLoadingOnProcess) {
                // Во всех случаях, когда не происходит пагинация - очищаем старый список и ставим тот, что пришёл
                recyclerAdapter.clearList()
            }
            // Если строка поиска пуста - просто добавлять приходящие новые списки в конец
            recyclerAdapter.addMovieCards(moviesDatabase)
        }
        isPaginationLoadingOnProcess = false
    }

    fun fullRefreshMoviesList() {
        isOffline = false
        isNeedRefreshLocalDB = true
        lastVisibleMovieCard = 0
        dispatchQueryToApi(INITIAL_PAGE_IN_RECYCLER)
    }

    fun startLoadingOnScroll(lastVisibleItemPosition: Int, itemsRemainingInList: Int, screenOrientation: Int) {
        lastVisibleMovieCard = lastVisibleItemPosition
        val relativeThreshold = if (moviesPerPage == 0) {
            nextPage  // Достигнут конец списка, избегается деление на ноль
        } else {
            ((lastVisibleItemPosition / moviesPerPage.toFloat()) + PAGINATION_RATIO).roundToInt()
        }

        val loadingThreshold =
            if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD * LOADING_THRESHOLD_MULTIPLIER_FOR_LANDSCAPE
            } else {
                RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD
            }

        if (!isPaginationLoadingOnProcess
            && itemsRemainingInList <= loadingThreshold
            && (isOffline || isNextPageCanBeDownloaded())
        ) {
            if (relativeThreshold > nextPage) {
                nextPage = relativeThreshold
                isPaginationLoadingOnProcess = true
                dispatchQueryToApi()
            }
        }
    }

    private fun getMoviesFromApi(page: Int = nextPage) {
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

    private fun getSearchedMoviesFromApi(query: CharSequence, page: Int = nextPage) {
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

    private fun isNextPageCanBeDownloaded() = nextPage <= totalPagesInLastQuery

    private fun dispatchQueryToApi(page: Int? = null) {
        if (isInSearchMode) {
            if (page != null) {
                getSearchedMoviesFromApi(lastSearch, page)
            } else {
                getSearchedMoviesFromApi(lastSearch)
            }
        } else {
            if (page != null) {
                getMoviesFromApi(page)
            } else {
                getMoviesFromApi()
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

        private const val INITIAL_PAGE_IN_RECYCLER = 1

        private const val RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD = 5
        private const val PAGINATION_RATIO = 0.9F
        private const val LOADING_THRESHOLD_MULTIPLIER_FOR_LANDSCAPE = 2
    }
}