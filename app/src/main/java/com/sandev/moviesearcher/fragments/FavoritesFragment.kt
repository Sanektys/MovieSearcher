package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.Gravity
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class FavoritesFragment : MoviesListFragment() {

    private var favoriteMoviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var isMovieNowNotFavorite: Boolean = false
    override var lastSearch: CharSequence? = null

    private var mainActivity: MainActivity? = null

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {
            resetExitReenterTransitionAnimations()
            mainActivity?.startDetailsFragment(movie, posterView)
        }
    }
    private val posterOnClickDummy = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {}
    }

    companion object {
        const val FAVORITES_DETAILS_RESULT_KEY = "FAVORITES_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_FAVORITE_KEY = "MOVIE_NOW_NOT_FAVORITE"

        private const val FAVORITE_MOVIES_RECYCLER_VIEW_STATE = "FavoriteMoviesRecylerViewState"

        private var favoriteMoviesRecyclerAdapter: MoviesRecyclerAdapter? = null
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_favorites, container, false)

        mainActivity = activity as MainActivity

        initializeViewsReferences(rootView)
        setAllAnimationTransition()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener(FAVORITES_DETAILS_RESULT_KEY, this) { _, bundle ->
            isMovieNowNotFavorite = bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)
        }

        initializeMovieRecyclerList()
        favoriteMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            FAVORITE_MOVIES_RECYCLER_VIEW_STATE))

        setupSearchBehavior(favoriteMoviesRecyclerAdapter, favoriteMovies)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FAVORITE_MOVIES_RECYCLER_VIEW_STATE, favoriteMoviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList() {
        if (favoriteMoviesRecyclerAdapter == null) {
            favoriteMoviesRecyclerAdapter = MoviesRecyclerAdapter()
            favoriteMoviesRecyclerAdapter?.setList(favoriteMovies)
        }
        // Пока не прошла анимация не обрабатывать клики на постеры
        favoriteMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClickDummy)

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.adapter = favoriteMoviesRecyclerAdapter

        favoriteMoviesRecyclerManager = recyclerView.layoutManager!!

        recyclerView.itemAnimator = MovieItemAnimator()

        recyclerView.postDelayed(  // Запускать удаление только после отрисовки анимации recycler
                resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()) {
            if (isMovieNowNotFavorite) {
                favoriteMoviesRecyclerAdapter?.removeLastClickedMovie()
                isMovieNowNotFavorite = false
                recyclerView.postDelayed((recyclerView.itemAnimator?.removeDuration ?: 0) +
                        (recyclerView.itemAnimator?.moveDuration ?: 0)) {
                    favoriteMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClick)
                }
            } else {
                favoriteMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClick)
            }
        }
        recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setAllAnimationTransition() {
        setTransitionAnimation(Gravity.END)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetExitReenterTransitionAnimations()
            Executors.newSingleThreadScheduledExecutor().apply {
                schedule({ setTransitionAnimation(Gravity.END) },
                    resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong(),
                    TimeUnit.MILLISECONDS)
                shutdown()
            }
        }
    }
}