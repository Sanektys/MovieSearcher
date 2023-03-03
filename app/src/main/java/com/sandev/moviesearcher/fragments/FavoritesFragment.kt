package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.favoriteMovies
import com.sandev.moviesearcher.movieListRecyclerView.itemAnimator.MovieItemAnimator


class FavoritesFragment : MoviesListFragment() {

    private var favoriteMoviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var favoriteMoviesRecyclerAdapter: MoviesRecyclerAdapter? = null
    private var isMovieNowNotFavorite: Boolean = false

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {
            (requireActivity() as MainActivity).startDetailsFragment(movie, posterView)
        }
    }
    private val posterOnClickDummy = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {}
    }

    companion object {
        const val DETAILS_RESULT_KEY = "DETAILS_RESULT"
        const val MOVIE_NOW_NOT_FAVORITE_KEY = "MOVIE_NOW_NOT_FAVORITE"

        private const val FAVORITE_MOVIES_RECYCLER_VIEW_STATE = "FavoriteMoviesRecylerViewState"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_favorites, container, false)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        returnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.no_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(DETAILS_RESULT_KEY, this) { _, bundle ->
            isMovieNowNotFavorite = bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)
        }

        initializeMovieRecyclerList(view)
        favoriteMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            FAVORITE_MOVIES_RECYCLER_VIEW_STATE))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FAVORITE_MOVIES_RECYCLER_VIEW_STATE, favoriteMoviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList(view: View) {
        if (favoriteMoviesRecyclerAdapter == null) {
            favoriteMoviesRecyclerAdapter = MoviesRecyclerAdapter()
            favoriteMoviesRecyclerAdapter!!.setList(favoriteMovies)
        }
        // Пока не прошла анимация не обрабатывать клики на постеры
        favoriteMoviesRecyclerAdapter!!.setPosterOnClickListener(posterOnClickDummy)

        val moviesListRecycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        moviesListRecycler.setHasFixedSize(true)
        moviesListRecycler.isNestedScrollingEnabled = false
        moviesListRecycler.adapter = favoriteMoviesRecyclerAdapter
        favoriteMoviesRecyclerManager = moviesListRecycler.layoutManager!!
        moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
        moviesListRecycler.itemAnimator = MovieItemAnimator()

        moviesListRecycler.postDelayed(  // Запускать удаление только после отрисовки анимации recycler
                resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()) {
            if (isMovieNowNotFavorite) {
                favoriteMoviesRecyclerAdapter!!.removeLastClickedMovie()
                isMovieNowNotFavorite = false
                moviesListRecycler.postDelayed(moviesListRecycler.itemAnimator!!.removeDuration +
                        moviesListRecycler.itemAnimator!!.moveDuration) {
                    favoriteMoviesRecyclerAdapter!!.setPosterOnClickListener(posterOnClick)
                }
            } else {
                favoriteMoviesRecyclerAdapter!!.setPosterOnClickListener(posterOnClick)
            }
        }
        moviesListRecycler.doOnPreDraw { startPostponedEnterTransition() }
    }
}