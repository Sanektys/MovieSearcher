package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
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

    protected var moviesPaginationOffset: Int = 0
        private set


    init {
        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE

        moviesListLiveData.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()
        }
    }


    override fun dispatchQueryToInteractor(query: String?, page: Int?) {
        if (page == INITIAL_PAGE_IN_RECYCLER) {
            moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE
            moviesPaginationOffset = 0
        }

        if (isInSearchMode) {
            getSearchedMoviesFromApi(
                query = query ?: "",
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

    private fun removeFromFavorite(movie: Movie) = savedMoviesComponent.interactor.removeFromList(movie)

    private fun removeMovieFromList() {
        if (lastClickedMovie != null) {
            removeFromFavorite(lastClickedMovie!!)
            recyclerAdapter.removeMovieCard(lastClickedMovie!!)

            lastClickedMovie = null

            --moviesPaginationOffset
        }
        isMovieMoreNotInSavedList = false
    }


    companion object {
        val RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY = App.instance.resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()
    }
}