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
import androidx.transition.Transition
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.cached_movies_feature.favorite_movies.FavoriteMoviesComponentViewModel
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentFavoritesBinding
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.FavoritesFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext


class FavoritesFragment : MoviesListFragment() {

    private var _viewModel: FavoritesFragmentViewModel? = null
    override val viewModel: FavoritesFragmentViewModel
        get() = _viewModel!!

    private var _binding: FragmentFavoritesBinding? = null
    private val binding: FragmentFavoritesBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null

    private val posterOnClick = object : MoviesRecyclerAdapter.OnPosterClickListener {
        override fun onClick(databaseMovie: DatabaseMovie, posterView: ShapeableImageView) {
            resetExitReenterTransitionAnimations()
            viewModel.lastClickedDatabaseMovie = databaseMovie
            mainActivity?.startDetailsFragment(databaseMovie, posterView)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val favoriteMoviesDatabaseComponentFactory
                = FavoriteMoviesComponentViewModel.ViewModelFactory(requireContext().applicationContext)
        val favoriteMoviesDatabaseComponent = ViewModelProvider(
            requireActivity(),
            favoriteMoviesDatabaseComponentFactory
        )[FavoriteMoviesComponentViewModel::class.java]

        val viewModelFactory = FavoritesFragmentViewModel.ViewModelFactory(favoriteMoviesDatabaseComponent.interactor)
        _viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[FavoritesFragmentViewModel::class.java]
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

        requireActivity().supportFragmentManager.setFragmentResultListener(
            FAVORITES_DETAILS_RESULT_KEY, this) { _, bundle ->
            if (bundle.getBoolean(MOVIE_NOW_NOT_FAVORITE_KEY)) {
                val job = CoroutineScope(Dispatchers.IO)
                job.launch {
                    viewModel.deleteMovieFromListAndDB()
                    job.cancel()
                }
            }
        }

        initializeMovieRecyclerList()
    }

    override fun onStop() {
        super.onStop()

        setExitTransitionAnimation(Gravity.END)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val job = CoroutineScope(EmptyCoroutineContext)
        job.launch {  // Если пользователь быстро нажимал "назад", то это предотвращает неудаление карточки
            viewModel.unblockMovieDeletion()
            job.cancel()
        }

        // Т.к. адаптер хранится во viewModel, то нужно при уничтожении вью его занулить, дабы избежать утечки памяти
        // (если этого не сделать, то адаптер будет ссылаться на само RecyclerView, поэтому оно НЕ соберётся GC.
        // А в самом RecyclerView есть ссылка на контекст до самой Activity, что приведёт к утечке и Activity тоже)
        val moviesRecycler = binding.moviesListRecycler

        (exitTransition as? Transition)?.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}

            override fun onTransitionEnd(transition: Transition) {
                (exitTransition as? Transition)?.removeListener(this)
                moviesRecycler.adapter = null
            }
        }) ?: moviesRecycler.setAdapter(null)

        _binding = null
        mainActivity = null
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
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        if (mainActivity?.previousFragment == HomeFragment::class ||
            mainActivity?.previousFragment == WatchLaterFragment::class) {
            // Запускать анимацию листания (появление сбоку) только если предыдущий фрагмент был списком фильмов
            setTransitionAnimation(Gravity.END)
            viewModel.unblockCallbackOnPosterClick(posterOnClick)
        } else {
            // Пока не прошла анимация не обрабатывать клики на постеры
            viewModel.blockCallbackOnPosterClickAndMovieDeletion()

            val layoutAnimationListener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    if (view == null) return
                    viewLifecycleOwner.lifecycleScope.launch {
                        launch {
                            binding.moviesListRecycler.layoutAnimationListener = null
                        }
                        launch(Dispatchers.Default) {
                            viewModel.unblockMovieDeletion()  // Запускать удаление карточки фильма только после отрисовки анимации recycler
                            viewModel.unblockCallbackOnPosterClick(posterOnClick)
                        }
                    }
                }
            }
            binding.moviesListRecycler.layoutAnimationListener = layoutAnimationListener
            binding.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.posters_appearance
            )
        }
    }


    companion object {
        const val FAVORITES_DETAILS_RESULT_KEY = "FAVORITES_DETAILS_RESULT"
        const val MOVIE_NOW_NOT_FAVORITE_KEY = "MOVIE_NOW_NOT_FAVORITE"
    }
}
