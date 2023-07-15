package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.SavedMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


abstract class SavedMoviesListViewModel : MoviesListFragmentViewModel() {

    lateinit var savedMoviesComponent: SavedMoviesComponentHolder

    final override val moviesList = MutableLiveData<List<Movie>>()

    var isMovieMoreNotInSavedList: Boolean = false
    var lastClickedMovie: Movie? = null

    var clickOnPosterCallbackSetupSynchronizeBlock: Channel<Nothing>? = null

    protected val movieDeletedObserver: Observer<Movie>
    protected val movieAddedObserver: Observer<Nothing?>

    private var moviesPaginationOffset: Int = 0


    init {
        isOffline = true
        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE

        movieDeletedObserver = Observer<Movie> { deletedMovie ->
            recyclerAdapter.removeMovieCard(deletedMovie)
            if (moviesPaginationOffset > 0) {
                --moviesPaginationOffset
            }
            unblockCallbackOnPosterClick()
        }
        movieAddedObserver = Observer<Nothing?> {
            if (recyclerAdapter.itemCount == 0) {
                dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
            } else {
                softResetPagination()
            }
        }

        moviesList.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()
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
        savedMoviesComponent.interactor.getDeletedMovie.removeObserver(movieDeletedObserver)
        savedMoviesComponent.interactor.getMovieAddedNotify.removeObserver(movieAddedObserver)
    }

    override fun dispatchQueryToInteractor(page: Int?) {
        if (page == INITIAL_PAGE_IN_RECYCLER) {
            hardResetPagination()
        }

        if (isInSearchMode) {
            getSearchedMoviesFromDB(
                query = lastSearch,
                offset = moviesPaginationOffset,
                moviesCount = moviesPerPage
            )
        } else {
            getMoviesFromDB(
                offset = moviesPaginationOffset,
                moviesCount = moviesPerPage
            )
        }
    }

    suspend fun checkForMovieDeletionNecessary() {
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

    private fun getMoviesFromDB(offset: Int, moviesCount: Int) = viewModelScope.launch {
        moviesList.postValue(
            savedMoviesComponent.interactor.getFewMoviesFromList(
                from = offset,
                moviesCount = moviesCount
            )
        )
    }

    private fun getSearchedMoviesFromDB(query: String, offset: Int, moviesCount: Int) = viewModelScope.launch {
        moviesList.postValue(
            savedMoviesComponent.interactor.getFewSearchedMoviesFromList(
                query = query,
                from = offset,
                moviesCount = moviesCount
            )
        )
    }

    private suspend fun removeFromSavedList(movie: Movie) {
        savedMoviesComponent.interactor.removeFromList(movie)
    }

    private suspend fun removeMovieFromList() {
        if (lastClickedMovie != null) {
            removeFromSavedList(lastClickedMovie!!)
            lastClickedMovie = null
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
        nextPage = INITIAL_PAGE_IN_RECYCLER
        moviesPerPage = SOFT_RESET_PAGINATION_MOVIES_PER_PAGE
        lastVisibleMovieCard = 0
    }


    companion object {
        const val SOFT_RESET_PAGINATION_MOVIES_PER_PAGE = 1
    }
}