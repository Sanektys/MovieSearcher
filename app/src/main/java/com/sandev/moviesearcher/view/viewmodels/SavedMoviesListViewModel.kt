package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.domain.components_holders.SavedMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.channels.Channel


abstract class SavedMoviesListViewModel : MoviesListFragmentViewModel() {

    lateinit var savedMoviesComponent: SavedMoviesComponentHolder

    final override val moviesList = MutableLiveData<List<DatabaseMovie>>()

    var isMovieMoreNotInSavedList: Boolean = false
    var lastClickedDatabaseMovie: DatabaseMovie? = null

    var clickOnPosterCallbackSetupSynchronizeBlock: Channel<Nothing>? = null

    protected val databaseMovieDeletedObserver: Observer<DatabaseMovie>
    protected val movieAddedObserver: Observer<Nothing?>

    private var moviesPaginationOffset: Int = 0
    private var isPaginationHardResetOnProcess: Boolean = false


    init {
        isOffline = true
        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE

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

        moviesList.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()

            if (isPaginationHardResetOnProcess) isPaginationHardResetOnProcess = false
        }
    }


    fun blockCallbackOnPosterClick() {
        clickOnPosterCallbackSetupSynchronizeBlock = Channel()
        recyclerAdapter.setPosterOnClickListener(null)
    }

    fun unblockCallbackOnPosterClick(callback: MoviesRecyclerAdapter.OnClickListener) {
        recyclerAdapter.setPosterOnClickListener(callback)
    }

    override fun onCleared() {
        savedMoviesComponent.interactor.getDeletedDatabaseMovie.removeObserver(databaseMovieDeletedObserver)
        savedMoviesComponent.interactor.getMovieAddedNotify.removeObserver(movieAddedObserver)
    }

    override fun dispatchQueryToInteractor(page: Int?) {
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
        disposable = savedMoviesComponent.interactor.getFewMoviesFromList(
            from = offset,
            moviesCount = MoviesListInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }

    private fun getSearchedMoviesFromDB(query: String, offset: Int) {
        var disposable: Disposable? = null
        disposable = savedMoviesComponent.interactor.getFewSearchedMoviesFromList(
                query = query,
                from = offset,
                moviesCount = MoviesListInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }

    private fun removeFromSavedList(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = savedMoviesComponent.interactor.removeFromList(databaseMovie).subscribe {
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
        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE
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