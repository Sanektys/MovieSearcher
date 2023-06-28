package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.SavedMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


abstract class SavedMoviesListViewModel : MoviesListFragmentViewModel() {

    lateinit var savedMoviesComponent: SavedMoviesComponentHolder

    final override val moviesListLiveData = MutableLiveData<List<Movie>>()

    var isMovieMoreNotInSavedList: Boolean = false
    var lastClickedMovie: Movie? = null

    protected val movieDeletedObserver: Observer<Movie>
    protected val movieAddedObserver: Observer<Nothing>

    private var moviesPaginationOffset: Int = 0


    init {
        isOffline = true
        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE

        movieDeletedObserver = Observer<Movie> { deletedMovie ->
            recyclerAdapter.removeMovieCard(deletedMovie)
            if (moviesPaginationOffset > 0) {
                --moviesPaginationOffset
            }
        }
        movieAddedObserver = Observer<Nothing> {
            if (recyclerAdapter.itemCount == 0) {
                dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
            } else {
                softResetPagination(moviesPerPage = 1)
            }
        }

        moviesListLiveData.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()
        }
    }


    override fun onCleared() {
        savedMoviesComponent.interactor.deletedMovieLiveData.removeObserver(movieDeletedObserver)
        savedMoviesComponent.interactor.movieAddedNotifyLiveData.removeObserver(movieAddedObserver)
    }

    override fun dispatchQueryToInteractor(query: String?, page: Int?) {
        if (page == INITIAL_PAGE_IN_RECYCLER) {
            hardResetPagination()
        }

        if (isInSearchMode) {
            getSearchedMoviesFromApi(
                query = query ?: lastSearch,
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

    fun setActivePosterOnClickListenerAndRemoveMovieIfNeeded(enabledPosterOnClickListener: MoviesRecyclerAdapter.OnClickListener) {
        Executors.newSingleThreadScheduledExecutor().apply {
            schedule({  // Запускать удаление только после отрисовки анимации recycler
                if (isMovieMoreNotInSavedList) {
                    removeMovieFromList()

                    Thread.sleep(MovieItemAnimator.REMOVE_DURATION)
                    recyclerAdapter.setPosterOnClickListener(enabledPosterOnClickListener)
                } else {
                    recyclerAdapter.setPosterOnClickListener(enabledPosterOnClickListener)
                }
            }, RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY, TimeUnit.MILLISECONDS)
        }
    }

    protected fun getMoviesFromDB(offset: Int, moviesCount: Int) {
        Executors.newSingleThreadExecutor().execute {
            moviesListLiveData.postValue(savedMoviesComponent.interactor.getFewMoviesFromList(
                from = offset,
                moviesCount = moviesCount
            ))
        }
    }

    private fun getSearchedMoviesFromApi(query: String, offset: Int, moviesCount: Int) {
        Executors.newSingleThreadExecutor().execute {
            moviesListLiveData.postValue(savedMoviesComponent.interactor.getFewSearchedMoviesFromList(
                query = query,
                from = offset,
                moviesCount = moviesCount
            ))
        }
    }

    private fun removeFromSavedList(movie: Movie) = savedMoviesComponent.interactor.removeFromList(movie)

    private fun removeMovieFromList() {
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

    private fun softResetPagination(moviesPerPage: Int = MoviesListInteractor.MOVIES_PER_PAGE) {
        nextPage = INITIAL_PAGE_IN_RECYCLER
        this.moviesPerPage = moviesPerPage
        lastVisibleMovieCard = 0
    }


    companion object {
        val RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY = App.instance.resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()
    }
}