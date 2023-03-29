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
import com.sandev.moviesearcher.fragments.FavoritesFragment
import com.sandev.moviesearcher.fragments.HomeFragment
import com.sandev.moviesearcher.fragments.MoviesListFragment
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.watchLaterMovies
import com.sandev.moviesearcher.movieListRecyclerView.itemAnimator.MovieItemAnimator
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class WatchLaterFragment : MoviesListFragment() {

    private var watchLaterMoviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var isMovieNowNotWatchLater: Boolean = false
    override var lastSearch: CharSequence? = null

    private lateinit var mainActivity: MainActivity

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {
            resetExitReenterTransitionAnimations()
            mainActivity.startDetailsFragment(movie, posterView)
        }
    }
    private val posterOnClickDummy = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {}
    }

    companion object {
        const val WATCH_LATER_DETAILS_RESULT_KEY = "WATCH_LATER_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_WATCH_LATER_KEY = "MOVIE_NOW_NOT_WATCH_LATER"

        private const val FAVORITE_MOVIES_RECYCLER_VIEW_STATE = "FavoriteMoviesRecylerViewState"

        private var watchLaterMoviesRecyclerAdapter: MoviesRecyclerAdapter? = null
        private var isLaunchedFromLeft = true
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_watch_later, container, false)

        mainActivity = activity as MainActivity
        val previousFragmentName = mainActivity.previousFragmentName
        if (previousFragmentName != DetailsFragment::class.qualifiedName) {
            isLaunchedFromLeft = if (previousFragmentName == HomeFragment::class.qualifiedName) {
                true
            } else if (previousFragmentName == FavoritesFragment::class.qualifiedName) {
                false
            } else {
                throw IllegalStateException("Incorrect previous fragment in WatchLaterFragment")
            }
        }

        initializeViewsReferences(rootView)
        setAllAnimationTransition()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(WATCH_LATER_DETAILS_RESULT_KEY, this) { _, bundle ->
            isMovieNowNotWatchLater = bundle.getBoolean(MOVIE_NOW_NOT_WATCH_LATER_KEY)
        }

        initializeMovieRecyclerList()
        watchLaterMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            FAVORITE_MOVIES_RECYCLER_VIEW_STATE))

        setupSearchBehavior(watchLaterMoviesRecyclerAdapter, watchLaterMovies)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FAVORITE_MOVIES_RECYCLER_VIEW_STATE, watchLaterMoviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList() {
        if (watchLaterMoviesRecyclerAdapter == null) {
            watchLaterMoviesRecyclerAdapter = MoviesRecyclerAdapter()
            watchLaterMoviesRecyclerAdapter?.setList(watchLaterMovies)
        }
        // Пока не прошла анимация не обрабатывать клики на постеры
        watchLaterMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClickDummy)

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.adapter = watchLaterMoviesRecyclerAdapter

        watchLaterMoviesRecyclerManager = recyclerView.layoutManager!!

        recyclerView.itemAnimator = MovieItemAnimator()

        recyclerView.postDelayed(  // Запускать удаление только после отрисовки анимации recycler
            resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance).toLong()) {
            if (isMovieNowNotWatchLater) {
                watchLaterMoviesRecyclerAdapter?.removeLastClickedMovie()
                isMovieNowNotWatchLater = false
                recyclerView.postDelayed((recyclerView.itemAnimator?.removeDuration ?: 0) +
                        (recyclerView.itemAnimator?.moveDuration ?: 0)) {
                    watchLaterMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClick)
                }
            } else {
                watchLaterMoviesRecyclerAdapter?.setPosterOnClickListener(posterOnClick)
            }
        }
        recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setAllAnimationTransition() {
        setTransitionAnimation()

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity.previousFragmentName == DetailsFragment::class.qualifiedName) {
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetExitReenterTransitionAnimations()
            Executors.newSingleThreadScheduledExecutor().apply {
                schedule({ setTransitionAnimation() },
                    resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong(),
                    TimeUnit.MILLISECONDS)
                shutdown()
            }
        }
    }

    private fun setTransitionAnimation() {
        if (isLaunchedFromLeft) {
            setTransitionAnimation(Gravity.END)
        } else {
            setTransitionAnimation(Gravity.START)
        }
    }

    fun prepareTransitionBeforeNewFragment(targetFragmentInLeft: Boolean) {
        if (targetFragmentInLeft) {
            setTransitionAnimation(Gravity.END)
        } else {
            setTransitionAnimation(Gravity.START)
        }
    }
}