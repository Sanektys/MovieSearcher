package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.google.android.material.search.SearchBar
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
    override var lastSearch: CharSequence? = null

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

        setAnimationTransition(rootView)

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

        setupSearchBehavior(favoriteMoviesRecyclerAdapter)
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
        moviesListRecycler.isNestedScrollingEnabled = true
        moviesListRecycler.adapter = favoriteMoviesRecyclerAdapter
        favoriteMoviesRecyclerManager = moviesListRecycler.layoutManager!!
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

    private fun setAnimationTransition(rootView: View) {
        setDefaultTransitionAnimation(rootView)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val recycler: RecyclerView = rootView.findViewById(R.id.movies_list_recycler)
        if ((activity as MainActivity).previousFragmentName == DetailsFragment::class.qualifiedName) {
            recycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
        }
    }

    override fun setDefaultTransitionAnimation(view: View) {
        val searchBar: SearchBar = view.findViewById(R.id.search_bar)
        val recycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        val duration = resources.getInteger(R.integer.general_animations_durations_fragment_transition).toLong()

        val recyclerTransition = Slide(Gravity.END).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addTarget(recycler)
        }
        val transitionSet = TransitionSet().addTransition(recyclerTransition)

        if (!isAppBarLifted) {
            val appBarTransition = Fade().apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
                addTarget(searchBar)
            }
            transitionSet.addTransition(appBarTransition)
        }
        enterTransition = transitionSet
        returnTransition = transitionSet
    }
}