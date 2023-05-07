package com.sandev.moviesearcher.view.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentFavoritesBinding
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.viewmodels.FavoritesFragmentViewModel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max


class FavoritesFragment : MoviesListFragment() {

    override val viewModel: FavoritesFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[FavoritesFragmentViewModel::class.java]
    }

    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null
    private var favoriteMoviesRecyclerManager: RecyclerView.LayoutManager? = null
    override var recyclerAdapter: MoviesRecyclerAdapter?
        set(value) {
            Companion.recyclerAdapter = value
        }
        get() = Companion.recyclerAdapter

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ImageView) {
            resetExitReenterTransitionAnimations()
            viewModel.lastClickedMovie = movie
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

        private var recyclerAdapter: MoviesRecyclerAdapter? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        initializeViewsReferences(binding.root)
        setAllAnimationTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.moviesListLiveData.observe(viewLifecycleOwner) { favoriteMovies ->
            moviesDatabase = favoriteMovies
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            FAVORITES_DETAILS_RESULT_KEY, this) { _, bundle ->
            viewModel.isMovieMoreNotFavorite = bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)
        }

        initializeMovieRecyclerList()
        favoriteMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            FAVORITE_MOVIES_RECYCLER_VIEW_STATE
        ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(FAVORITE_MOVIES_RECYCLER_VIEW_STATE, favoriteMoviesRecyclerManager?.onSaveInstanceState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        favoriteMoviesRecyclerManager = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isChangingConfigurations == false) {
            recyclerAdapter = null
        }
    }

    private fun initializeMovieRecyclerList() {
        if (recyclerAdapter == null) {
            recyclerAdapter = MoviesRecyclerAdapter()
            initializeRecyclerAdapter()
        }
        // Пока не прошла анимация не обрабатывать клики на постеры
        recyclerAdapter?.setPosterOnClickListener(posterOnClickDummy)

        binding.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = recyclerAdapter

            favoriteMoviesRecyclerManager = layoutManager!!

            itemAnimator = MovieItemAnimator()

            postDelayed(  // Запускать удаление только после отрисовки анимации recycler
                resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance)
                    .toLong()
            ) {
                if (viewModel.isMovieMoreNotFavorite) {
                    if (viewModel.lastClickedMovie != null) {
                        viewModel.removeFromFavorite(viewModel.lastClickedMovie!!)
                        viewModel.lastClickedMovie = null
                    }
                    viewModel.isMovieMoreNotFavorite = false
                    postDelayed(
                        max(itemAnimator?.removeDuration ?: 0, itemAnimator?.moveDuration ?: 0)
                    ) {
                        recyclerAdapter?.setPosterOnClickListener(posterOnClick)
                    }
                } else {
                    recyclerAdapter?.setPosterOnClickListener(posterOnClick)
                }
            }
            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setAllAnimationTransition() {
        setTransitionAnimation(Gravity.END)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            binding.moviesListRecycler.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
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