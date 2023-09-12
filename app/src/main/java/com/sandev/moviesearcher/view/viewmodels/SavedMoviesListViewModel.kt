package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.channels.Channel


abstract class SavedMoviesListViewModel(private val cachedMoviesInteractor: CachedMoviesInteractor)
    : MoviesListFragmentViewModel() {

    final override val moviesList = MutableLiveData<List<DatabaseMovie>>()

    var isMovieMoreNotInSavedList: Boolean = false
    var lastClickedDatabaseMovie: DatabaseMovie? = null

    var clickOnPosterCallbackSetupSynchronizeBlock: Channel<Nothing>? = null

    private val databaseMovieDeletedObserver: Observer<DatabaseMovie>
    private val movieAddedObserver: Observer<Nothing?>

    private var moviesPaginationOffset: Int = 0
    private var isPaginationHardResetOnProcess: Boolean = false


    init {
        isOffline = true
        moviesPerPage = CachedMoviesInteractor.MOVIES_PER_PAGE

        databaseMovieDeletedObserver = Observer<DatabaseMovie> { deletedMovie ->
            val isMovieDeleted = recyclerAdapter.removeMovieCard(deletedMovie)
            if (isMovieDeleted && moviesPaginationOffset > 0) {
                --moviesPaginationOffset
                nextPage = INITIAL_PAGE_IN_RECYCLER
            }
            unblockCallbackOnPosterClick()
        }
        movieAddedObserver = Observer<Nothing?> {
            if (recyclerAdapter.itemCount == 0) {
                dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
            } else {
                // Сохранённые списки не зависят от nextPage напрямую, только от moviesPaginationOffset,
                // поэтому сбрасываем страницу при каждом обновлении чтобы пагинация загружала новые страницы
                nextPage = INITIAL_PAGE_IN_RECYCLER
                if (moviesPerPage == 0) {  // Пагинация в последний раз дошла до конца списка
                    softResetPagination()
                }
            }
        }

        cachedMoviesInteractor.getDeletedDatabaseMovie.observeForever(databaseMovieDeletedObserver)
        cachedMoviesInteractor.getMovieAddedNotify.observeForever(movieAddedObserver)

        moviesList.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()

            if (isPaginationHardResetOnProcess) isPaginationHardResetOnProcess = false
        }

        dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
    }


    fun blockCallbackOnPosterClick() {
        clickOnPosterCallbackSetupSynchronizeBlock = Channel()
        recyclerAdapter.setPosterOnClickListener(null)
    }

    fun unblockCallbackOnPosterClick(callback: MoviesRecyclerAdapter.OnPosterClickListener) {
        recyclerAdapter.setPosterOnClickListener(callback)
    }

    override fun onCleared() {
        cachedMoviesInteractor.getDeletedDatabaseMovie.removeObserver(databaseMovieDeletedObserver)
        cachedMoviesInteractor.getMovieAddedNotify.removeObserver(movieAddedObserver)
    }

    final override fun dispatchQueryToInteractor(page: Int?) {
        // Избегать двойной инициализации при создании экземпляра viewmodel
        // вызовом dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER) в блоке init и коллбэке observer'а
        if (isPaginationHardResetOnProcess) return

        if (page == INITIAL_PAGE_IN_RECYCLER) {
            isPaginationHardResetOnProcess = true
            hardResetPagination()
        }

        if (isInSearchMode) {
            getSearchedMoviesFromDB(
                query = lastSearch,
                offset = moviesPaginationOffset
            )
        } else {
            getMoviesFromDB(
                offset = moviesPaginationOffset
            )
        }
    }

    fun checkForMovieDeletionNecessary() {
        if (isMovieMoreNotInSavedList) {
            removeMovieFromList()
        } else {
            unblockCallbackOnPosterClick()
        }
    }

    private fun unblockCallbackOnPosterClick() {
        clickOnPosterCallbackSetupSynchronizeBlock?.run {
            cancel()
            clickOnPosterCallbackSetupSynchronizeBlock = null
        }
    }

    private fun getMoviesFromDB(offset: Int) {
        var disposable: Disposable? = null
        disposable = cachedMoviesInteractor.getFewMoviesFromList(
            from = offset,
            moviesCount = CachedMoviesInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }

    private fun getSearchedMoviesFromDB(query: String, offset: Int) {
        var disposable: Disposable? = null
        disposable = cachedMoviesInteractor.getFewSearchedMoviesFromList(
                query = query,
                from = offset,
                moviesCount = CachedMoviesInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }

    private fun removeFromSavedList(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = cachedMoviesInteractor.removeFromList(databaseMovie).subscribe {
            disposable?.dispose()
        }
    }

    private fun removeMovieFromList() {
        if (lastClickedDatabaseMovie != null) {
            removeFromSavedList(lastClickedDatabaseMovie!!)
            lastClickedDatabaseMovie = null
        }
        isMovieMoreNotInSavedList = false
    }

    private fun hardResetPagination() {
        nextPage = INITIAL_PAGE_IN_RECYCLER
        moviesPerPage = CachedMoviesInteractor.MOVIES_PER_PAGE
        lastVisibleMovieCard = 0
        moviesPaginationOffset = 0
    }

    private fun softResetPagination() {
        moviesPerPage = MOCK_MOVIES_PER_PAGE_COUNT_FOR_TRIGGER_TO_NEXT_PAGE_LOAD
        lastVisibleMovieCard = 0
    }


    companion object {
        const val MOCK_MOVIES_PER_PAGE_COUNT_FOR_TRIGGER_TO_NEXT_PAGE_LOAD = 1
    }
}