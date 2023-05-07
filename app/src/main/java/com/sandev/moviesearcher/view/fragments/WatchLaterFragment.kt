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
import com.sandev.moviesearcher.databinding.FragmentWatchLaterBinding
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.WatchLaterFragmentViewModel
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.max


class WatchLaterFragment : MoviesListFragment() {

    override val viewModel: WatchLaterFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[WatchLaterFragmentViewModel::class.java]
    }

    private var _binding: FragmentWatchLaterBinding? = null
    private val binding: FragmentWatchLaterBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null
    private var watchLaterMoviesRecyclerManager: RecyclerView.LayoutManager? = null
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
        const val WATCH_LATER_DETAILS_RESULT_KEY = "WATCH_LATER_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_WATCH_LATER_KEY = "MOVIE_NOW_NOT_WATCH_LATER"

        private const val WATCH_LATER_MOVIES_RECYCLER_VIEW_STATE = "WatchLaterMoviesRecylerViewState"

        private var isLaunchedFromLeft = true

        private var recyclerAdapter: MoviesRecyclerAdapter? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity
        val previousFragmentName = mainActivity?.previousFragmentName
        if (previousFragmentName != DetailsFragment::class.qualifiedName) {
            if (previousFragmentName == HomeFragment::class.qualifiedName) {
                isLaunchedFromLeft = true
            } else if (previousFragmentName == FavoritesFragment::class.qualifiedName) {
                isLaunchedFromLeft = false
            }
        }

        initializeViewsReferences(binding.root)
        setAllAnimationTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.moviesListLiveData.observe(viewLifecycleOwner) { watchLaterMovies ->
            moviesDatabase = watchLaterMovies
        }

        requireActivity().supportFragmentManager.setFragmentResultListener(
            WATCH_LATER_DETAILS_RESULT_KEY, this) { _, bundle ->
            viewModel.isMovieMoreNotWatchLater = bundle.getBoolean(MOVIE_NOW_NOT_WATCH_LATER_KEY)
        }

        initializeMovieRecyclerList()
        watchLaterMoviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            WATCH_LATER_MOVIES_RECYCLER_VIEW_STATE
        ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(WATCH_LATER_MOVIES_RECYCLER_VIEW_STATE, watchLaterMoviesRecyclerManager?.onSaveInstanceState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        watchLaterMoviesRecyclerManager = null
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

            watchLaterMoviesRecyclerManager = layoutManager!!

            itemAnimator = MovieItemAnimator()

            postDelayed(  // Запускать удаление только после отрисовки анимации recycler
                resources.getInteger(R.integer.fragment_favorites_delay_recyclerViewAppearance)
                    .toLong()
            ) {
                if (viewModel.isMovieMoreNotWatchLater) {
                    if (viewModel.lastClickedMovie != null) {
                        viewModel.removeFromWatchLater(viewModel.lastClickedMovie!!)
                        viewModel.lastClickedMovie = null
                    }
                    viewModel.isMovieMoreNotWatchLater = false
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
        setTransitionAnimation()

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            binding.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(),
                R.anim.posters_appearance
            )
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