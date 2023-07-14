package com.sandev.moviesearcher.view.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.databinding.FragmentWatchLaterBinding
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.WatchLaterFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class WatchLaterFragment : MoviesListFragment() {

    override val viewModel: WatchLaterFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[WatchLaterFragmentViewModel::class.java]
    }

    private var _binding: FragmentWatchLaterBinding? = null
    private val binding: FragmentWatchLaterBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(movie: Movie, posterView: ShapeableImageView) {
            resetExitReenterTransitionAnimations()
            viewModel.lastClickedMovie = movie
            mainActivity?.startDetailsFragment(movie, posterView)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchLaterBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        viewModel.isMovieMoreNotInSavedList = false

        val previousFragmentName = mainActivity?.previousFragmentName
        if (previousFragmentName != DetailsFragment::class.qualifiedName) {
            if (previousFragmentName == HomeFragment::class.qualifiedName) {
                viewModel.isLaunchedFromLeft = true
            } else if (previousFragmentName == FavoritesFragment::class.qualifiedName) {
                viewModel.isLaunchedFromLeft = false
            }
        }

        initializeViewsReferences(binding.root)
        setAllAnimationTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener(
            WATCH_LATER_DETAILS_RESULT_KEY, this) { _, bundle ->
            viewModel.isMovieMoreNotInSavedList = bundle.getBoolean(MOVIE_NOW_NOT_WATCH_LATER_KEY)
        }

        initializeMovieRecyclerList()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        runBlocking {   // Если пользователь быстро нажимал "назад", то это предотвращает неудаление карточки
            viewModel.checkForMovieDeletionNecessary()
        }

        _binding = null
    }

    fun prepareTransitionBeforeNewFragment(targetFragmentInLeft: Boolean) {
        if (targetFragmentInLeft) {
            setTransitionAnimation(Gravity.END)
        } else {
            setTransitionAnimation(Gravity.START)
        }
    }

    private fun initializeMovieRecyclerList() {
        binding.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = viewModel.recyclerAdapter

            itemAnimator = MovieItemAnimator()

            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setAllAnimationTransition() {
        setTransitionAnimation()

        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Пока не прошла анимация не обрабатывать клики на постеры
            viewModel.blockCallbackOnPosterClick()

            resetExitReenterTransitionAnimations()

            val layoutAnimationListener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (view == null) return
                    viewLifecycleOwner.lifecycleScope.launch {
                        launch {
                            binding.moviesListRecycler.layoutAnimationListener = null
                            setTransitionAnimation(Gravity.END)
                        }
                        launch(Dispatchers.Default) {  // Запускать удаление карточки фильма только после отрисовки анимации recycler
                            viewModel.checkForMovieDeletionNecessary()
                            viewModel.clickOnPosterCallbackSetupSynchronizeBlock?.receiveCatching()
                            viewModel.unblockCallbackOnPosterClick(posterOnClick)
                        }
                    }
                }
            }
            binding.moviesListRecycler.layoutAnimationListener = layoutAnimationListener
            binding.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.posters_appearance
            )
        } else {
            viewModel.unblockCallbackOnPosterClick(posterOnClick)
        }
    }

    private fun setTransitionAnimation() {
        if (viewModel.isLaunchedFromLeft) {
            setTransitionAnimation(Gravity.END)
        } else {
            setTransitionAnimation(Gravity.START)
        }
    }


    companion object {
        const val WATCH_LATER_DETAILS_RESULT_KEY = "WATCH_LATER_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_WATCH_LATER_KEY = "MOVIE_NOW_NOT_WATCH_LATER"
    }
}