package com.sandev.moviesearcher.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.cached_movies_feature.watch_later_movies.WatchLaterMoviesComponentViewModel
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentWatchLaterBinding
import com.sandev.moviesearcher.utils.rv_animators.MovieItemAnimator
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.rv_adapters.WatchLaterRecyclerAdapter
import com.sandev.moviesearcher.view.rv_viewholders.WatchLaterMovieViewHolder
import com.sandev.moviesearcher.view.viewmodels.WatchLaterFragmentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext


class WatchLaterFragment : MoviesListFragment() {

    private var _viewModel: WatchLaterFragmentViewModel? = null
    override val viewModel: WatchLaterFragmentViewModel
        get() = _viewModel!!

    private var _binding: FragmentWatchLaterBinding? = null
    private val binding: FragmentWatchLaterBinding
        get() = _binding!!

    private var mainActivity: MainActivity? = null

    private val posterOnClick = object : MoviesRecyclerAdapter.OnPosterClickListener {
        override fun onClick(databaseMovie: DatabaseMovie, posterView: ShapeableImageView) {
            resetExitReenterTransitionAnimations()
            viewModel.lastClickedDatabaseMovie = databaseMovie
            mainActivity?.startDetailsFragment(databaseMovie, posterView)
        }
    }
    private val scheduleButtonOnClick by lazy { initializeScheduleNotificationButton() }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val watchLaterMoviesDatabaseComponentFactory
                = WatchLaterMoviesComponentViewModel.ViewModelFactory(context)
        val watchLaterMoviesDatabaseComponent = ViewModelProvider(
            requireActivity(),
            watchLaterMoviesDatabaseComponentFactory
        )[WatchLaterMoviesComponentViewModel::class.java]

        val viewModelFactory = WatchLaterFragmentViewModel.ViewModelFactory(watchLaterMoviesDatabaseComponent.interactor)
        _viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[WatchLaterFragmentViewModel::class.java]
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
            if (bundle.getBoolean(MOVIE_NOW_NOT_WATCH_LATER_KEY)) {
                val job = CoroutineScope(Dispatchers.IO)
                job.launch {
                    viewModel.deleteMovieFromListAndDB()
                    job.cancel()
                }
            }
        }

        initializeMovieRecyclerList()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val job = CoroutineScope(EmptyCoroutineContext)
        job.launch {  // Если пользователь быстро нажимал "назад", то это предотвращает неудаление карточки
            viewModel.unblockMovieDeletion()
            job.cancel()
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

    private fun initializeScheduleNotificationButton(): WatchLaterRecyclerAdapter.ScheduleNotificationButtonClick {
        return object : WatchLaterRecyclerAdapter.ScheduleNotificationButtonClick {
            override fun onButtonClick(viewHolder: WatchLaterMovieViewHolder, movie: WatchLaterDatabaseMovie) {
                val popupMenuTheme = ContextThemeWrapper(requireContext(), R.style.Widget_MovieSearcher_PopupMenu)
                PopupMenu(
                    requireContext(), viewHolder.scheduleButton, Gravity.BOTTOM,
                    0, popupMenuTheme.themeResId
                ).apply {
                    menuInflater.inflate(R.menu.watch_later_movie_card_popup_menu, menu)

                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.watch_later_card_popup_change_button -> {
                                viewModel.rescheduleMovieNotificationDate(
                                    activity = requireActivity(),
                                    viewHolder = viewHolder,
                                    movie = movie,
                                    datePickerTitle = R.string.watch_later_fragment_popup_menu_date_picker_title,
                                    timePickerTitle = R.string.watch_later_fragment_popup_menu_time_picker_title
                                )
                                true
                            }

                            R.id.watch_later_card_popup_remove_button -> {
                                viewModel.removeMovieFromWatchLaterListAndSchedule(
                                    context = requireContext(),
                                    movie = movie
                                )
                                true
                            }

                            else -> false
                        }
                    }
                    show()
                }
            }
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
            // Пока не прошла анимация не обрабатывать клики на постеры и кнопки настройки нотификаций
            viewModel.blockOnClickCallbacksOnMovieCardElementsAndMovieDeletion()

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
                            viewModel.unblockMovieDeletion()
                            viewModel.unblockOnClickCallbacksOnMovieCardElements(posterOnClick, scheduleButtonOnClick)
                        }
                    }
                }
            }
            binding.moviesListRecycler.layoutAnimationListener = layoutAnimationListener
            binding.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.posters_appearance
            )
        } else {
            viewModel.unblockOnClickCallbacksOnMovieCardElements(posterOnClick, scheduleButtonOnClick)
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