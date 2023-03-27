package com.sandev.moviesearcher

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
import com.sandev.moviesearcher.fragments.DetailsFragment
import com.sandev.moviesearcher.fragments.MoviesListFragment
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.watchLaterMovies
import com.sandev.moviesearcher.movieListRecyclerView.itemAnimator.MovieItemAnimator


class WatchLaterFragment : MoviesListFragment() {

    private var favoriteMoviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var favoriteMoviesRecyclerAdapter: MoviesRecyclerAdapter? = null
    private var isMovieNowNotWatchLater: Boolean = false
    override var lastSearch: CharSequence? = null

    private lateinit var mainActivity: MainActivity

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {
            mainActivity.startDetailsFragment(movie, posterView)
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
        val rootView = layoutInflater.inflate(R.layout.fragment_watch_later, container, false)

        mainActivity = activity as MainActivity

        initializeViewsReferences(rootView)
        setAllAnimationTransition()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(DETAILS_RESULT_KEY, this) { _, bundle ->
            isMovieNowNotWatchLater = bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)
        }

        initializeMovieRecyclerList()
        favoriteMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            FAVORITE_MOVIES_RECYCLER_VIEW_STATE))

        setupSearchBehavior(favoriteMoviesRecyclerAdapter, watchLaterMovies)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FAVORITE_MOVIES_RECYCLER_VIEW_STATE, favoriteMoviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList() {
        if (favoriteMoviesRecyclerAdapter == null) {
            favoriteMoviesRecyclerAdapter = MoviesRecyclerAdapter()
            favoriteMoviesRecyclerAdapter?.setList(watchLaterMovies)
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
            if (isMovieNowNotWatchLater) {
                favoriteMoviesRecyclerAdapter?.removeLastClickedMovie()
                isMovieNowNotWatchLater = false
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
        setTransitionAnimation(Gravity.END, false)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity.previousFragmentName == DetailsFragment::class.qualifiedName) {
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
        }
    }
}