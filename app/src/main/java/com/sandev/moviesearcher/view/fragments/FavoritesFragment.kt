package com.sandev.moviesearcher.view.fragments

import android.content.Context
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
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.cached_movies_feature.favorite_movies.FavoriteMoviesComponentViewModel
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentFavoritesBinding
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.FavoritesFragmentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class FavoritesFragment : MoviesListFragment() {

    private var _viewModel: FavoritesFragmentViewModel? = null
    override val viewModel: FavoritesFragmentViewModel = _viewModel!!

    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null

    private val posterOnClick = object : MoviesRecyclerAdapter.OnClickListener {
        override fun onClick(databaseMovie: DatabaseMovie, posterView: ShapeableImageView) {
            resetExitReenterTransitionAnimations()
            viewModel.lastClickedDatabaseMovie = databaseMovie
            mainActivity?.startDetailsFragment(databaseMovie, posterView)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val favoriteMoviesDatabaseComponent = ViewModelProvider(requireActivity())[FavoriteMoviesComponentViewModel::class.java]

        val viewModelFactory = FavoritesFragmentViewModel.ViewModelFactory(favoriteMoviesDatabaseComponent.interactor)
        _viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FavoritesFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        viewModel.isMovieMoreNotInSavedList = false

        initializeViewsReferences(binding.root)
        setAllAnimationTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().supportFragmentManager.setFragmentResultListener(
            FAVORITES_DETAILS_RESULT_KEY, this) { _, bundle ->
            viewModel.isMovieMoreNotInSavedList = bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)
        }

        initializeMovieRecyclerList()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        runBlocking {  // Если пользователь быстро нажимал "назад", то это предотвращает неудаление карточки
            viewModel.checkForMovieDeletionNecessary()
        }

        _binding = null
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
        setTransitionAnimation(Gravity.END)

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
                        launch(Dispatchers.Default) {
                            viewModel.checkForMovieDeletionNecessary()  // Запускать удаление карточки фильма только после отрисовки анимации recycler
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


    companion object {
        const val FAVORITES_DETAILS_RESULT_KEY = "FAVORITES_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_FAVORITE_KEY = "MOVIE_NOW_NOT_FAVORITE"
    }
}
