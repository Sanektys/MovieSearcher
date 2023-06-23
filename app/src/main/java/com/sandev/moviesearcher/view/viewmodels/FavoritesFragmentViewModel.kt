package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class FavoritesFragmentViewModel : MoviesListFragmentViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder

    override val moviesListLiveData: LiveData<List<Movie>>
    override val moviesObserver: Observer<List<Movie>>

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    var isMovieMoreNotFavorite: Boolean = false
    var lastClickedMovie: Movie? = null


    init {
        App.instance.getAppComponent().inject(this)

        moviesListLiveData = favoritesMoviesComponent.interactor.getAllFromList()

        moviesObserver = Observer<List<Movie>> { newList ->
            moviesDatabase = newList.toList()
        }
        moviesListLiveData.observeForever(moviesObserver)

    }


    override fun onCleared() {
        moviesListLiveData.removeObserver(moviesObserver)
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, getAllMovies())
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

    private fun addToFavorite(movie: Movie) = favoritesMoviesComponent.interactor.addToList(movie)

    private fun removeFromFavorite(movie: Movie) = favoritesMoviesComponent.interactor.removeFromList(movie)

    private fun removeMovieFromList() {
        if (lastClickedMovie != null) {
            removeFromFavorite(lastClickedMovie!!)
            lastClickedMovie = null
        }
        isMovieMoreNotFavorite = false
    }


    companion object {
        private var lastSearch: String = ""

        val RECYCLER_VIEW_APPEARANCE_ANIMATION_DELAY = App.instance.resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()
    }
}