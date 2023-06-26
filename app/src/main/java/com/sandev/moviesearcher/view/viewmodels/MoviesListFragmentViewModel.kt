package com.sandev.moviesearcher.view.viewmodels

import android.content.res.Configuration
import android.view.Gravity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import kotlin.math.roundToInt


abstract class MoviesListFragmentViewModel : ViewModel() {

    abstract val moviesListLiveData: LiveData<List<Movie>>

    abstract var lastSearch: String
        protected set

    val recyclerAdapter: MoviesRecyclerAdapter = MoviesRecyclerAdapter()

    var isOffline: Boolean = false
        protected set
    protected var isNeedRefreshLocalDB: Boolean = false
    var lastVisibleMovieCard: Int = 0
        protected set

    private var isPaginationLoadingOnProcess: Boolean = false
    protected var nextPage: Int = 1
    protected var moviesPerPage: Int = 0
    protected var totalPagesInLastQuery = 1

    protected abstract var isInSearchMode: Boolean

    protected var moviesDatabase: List<Movie> = emptyList()
        set(value) {
            field = value
            initializeRecyclerAdapterList()
        }

    var lastSlideGravity = Gravity.TOP


    protected abstract fun dispatchQueryToInteractor(query: String? = null, page: Int? = null)

    fun fullRefreshMoviesList() {
        isOffline = false
        isNeedRefreshLocalDB = true
        lastVisibleMovieCard = 0
        dispatchQueryToInteractor(lastSearch, INITIAL_PAGE_IN_RECYCLER)
    }

    fun searchInSearchView(query: String) {
        if (query == lastSearch) return

        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            if (!isInSearchMode) {
                isInSearchMode = true
                recyclerAdapter.clearList()
            }
            dispatchQueryToInteractor(query = query, page = INITIAL_PAGE_IN_RECYCLER)
        } else if (query.isEmpty()) {
            if (isInSearchMode) {
                isInSearchMode = false
                recyclerAdapter.clearList()
            }
            dispatchQueryToInteractor(query = null, page = INITIAL_PAGE_IN_RECYCLER)
        }
        lastSearch = query
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
                dispatchQueryToInteractor()
            }
        }
    }

    private fun initializeRecyclerAdapterList() {
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

    private fun isNextPageCanBeDownloaded() = nextPage <= totalPagesInLastQuery


    interface ApiCallback {
        fun onSuccess(movies: List<Movie>, totalPages: Int)
        fun onFailure()
    }


    companion object {
        const val SEARCH_SYMBOLS_THRESHOLD = 2

        const val INITIAL_PAGE_IN_RECYCLER = 1

        private const val RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD = 5
        private const val PAGINATION_RATIO = 0.9F
        private const val LOADING_THRESHOLD_MULTIPLIER_FOR_LANDSCAPE = 2
    }
}