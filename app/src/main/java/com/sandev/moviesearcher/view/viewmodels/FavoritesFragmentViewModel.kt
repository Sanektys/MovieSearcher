package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FavoritesFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) {
            Companion.isInSearchMode = value
        }
        get() = Companion.isInSearchMode

    var isMovieMoreNotFavorite: Boolean = false
    var lastClickedMovie: Movie? = null

    private var moviesPaginationOffset: Int = 0


    init {
        App.instance.getAppComponent().inject(this)

        moviesPerPage = MoviesListInteractor.MOVIES_PER_PAGE

        moviesListLiveData.observeForever { newList ->
            moviesPerPage = newList.size
            moviesPaginationOffset += moviesPerPage

            moviesDatabase = newList.toList()
        }

        getMoviesFromDB(
            offset = moviesPaginationOffset,
            moviesCount = moviesPerPage
        )
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
                if (isMovieMoreNotFavorite) {
                    removeMovieFromList()

                    Thread.sleep(MovieItemAnimator.REMOVE_DURATION)
                    recyclerAdapter.setPosterOnClickListener(enabledPosterOnClickListener)
                } else {
                    recyclerAdapter.setPosterOnClickListener(enabledPosterOnClickListener)
                }
            }, RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY, TimeUnit.MILLISECONDS)
        }
    }

    private fun getMoviesFromDB(offset: Int, moviesCount: Int) {
        Executors.newSingleThreadExecutor().execute {
            moviesListLiveData.postValue(favoritesMoviesComponent.interactor.getFewMoviesFromList(
                from = offset,
                moviesCount = moviesCount
            ))
        }
    }

    private fun getSearchedMoviesFromApi(query: String, offset: Int, moviesCount: Int) {
        Executors.newSingleThreadExecutor().execute {
            moviesListLiveData.postValue(favoritesMoviesComponent.interactor.getFewSearchedMoviesFromList(
                query = query,
                from = offset,
                moviesCount = moviesCount
            ))
        }
    }

    private fun removeFromFavorite(movie: Movie) = favoritesMoviesComponent.interactor.removeFromList(movie)

    private fun removeMovieFromList() {
        if (lastClickedMovie != null) {
            removeFromFavorite(lastClickedMovie!!)
            recyclerAdapter.removeMovieCard(lastClickedMovie!!)

            lastClickedMovie = null

            --moviesPaginationOffset
        }
        isMovieMoreNotFavorite = false
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false

        val RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY = App.instance.resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()
    }
}