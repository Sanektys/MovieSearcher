package com.sandev.moviesearcher.view.fragments

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentHomeBinding
import com.sandev.moviesearcher.databinding.MergeFragmentHomeContentBinding
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import com.sandev.tmdb_feature.TmdbComponentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : MoviesListFragment() {

    private var _viewModel: HomeFragmentViewModel? = null
    override val viewModel: HomeFragmentViewModel
        get() = _viewModel!!

    private var _bindingFull: MergeFragmentHomeContentBinding? = null
    private val bindingFull: MergeFragmentHomeContentBinding
        get() = _bindingFull!!
    private var _bindingBlank: FragmentHomeBinding? = null
    private val bindingBlank: FragmentHomeBinding
        get() = _bindingBlank!!

    private var mainActivity: MainActivity? = null

    private var snackbar: Snackbar? = null

    private val backStackChangedListener: OnBackStackChangedListener


    init {
        backStackChangedListener = createChildFragmentsChangeListener()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val tmdbComponent = ViewModelProvider(requireActivity())[TmdbComponentViewModel::class.java]

        val viewModelFactory = HomeFragmentViewModel.ViewModelFactory(tmdbComponent.interactor)
        _viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[HomeFragmentViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingBlank = FragmentHomeBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity

        setAllTransitionAnimation()

        return bindingBlank.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeSwipeRefreshLayout()
        initializeSwipeRefreshStateListener()
        prepareErrorConnectionSnackbar()

        childFragmentManager.addOnBackStackChangedListener(backStackChangedListener)

        super.onViewCreated(view, savedInstanceState)

        setupViewModelObserving()

        initializeMovieRecycler()
    }

    override fun onStop() {
        super.onStop()

        setExitTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        bindingFull.moviesListRecycler.clearOnChildAttachStateChangeListeners()
        childFragmentManager.removeOnBackStackChangedListener(backStackChangedListener)

        // Т.к. адаптер хранится во viewModel, то нужно при уничтожении вью его занулить, дабы избежать утечки памяти
        // (если этого не сделать, то адаптер будет ссылаться на само RecyclerView, поэтому оно НЕ соберётся GC.
        // А в самом RecyclerView есть ссылка на контекст до самой Activity, что приведёт к утечке и Activity тоже)
        val moviesRecycler = bindingFull.moviesListRecycler

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

        _bindingFull = null
        _bindingBlank = null
        mainActivity = null
    }

    override fun setRecyclerViewAppearance(view: View) {
        super.setRecyclerViewAppearance(bindingFull.swipeRefresh)
    }

    private fun initializeMovieRecycler() {
        viewModel.recyclerAdapter.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnPosterClickListener {
            override fun onClick(databaseMovie: DatabaseMovie, posterView: ShapeableImageView) {
                mainActivity?.startDetailsFragment(databaseMovie, posterView)
            }
        })

        bindingFull.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = viewModel.recyclerAdapter

            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun prepareErrorConnectionSnackbar() {
        snackbar = Snackbar.make(
            bindingFull.root,
            getString(R.string.activity_main_snackbar_message_failure_on_load_data),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            animationMode = ANIMATION_MODE_FADE
            behavior = object : BaseTransientBottomBar.Behavior() {
                override fun canSwipeDismissView(child: View) = false
            }
            setAction(getString(R.string.activity_main_snackbar_button_retry)) {
                bindingFull.swipeRefresh.isRefreshing = true
                viewModel.fullRefreshMoviesList()
            }
        }
    }

    private fun createChildFragmentsChangeListener() = OnBackStackChangedListener {
        if (childFragmentManager.fragments.size == 0) {
            if (viewModel.onFailureFlag) {
                snackbar?.show()
            }
        } else {
            snackbar?.dismiss()
        }
    }

    private fun initializeSwipeRefreshLayout() {
        bindingFull.swipeRefresh.setOnRefreshListener {
            viewModel.fullRefreshMoviesList()
        }
    }

    private fun initializeSwipeRefreshStateListener() {
        viewModel.getSwipeRefreshState.observe(viewLifecycleOwner) { isActive ->
            _bindingFull?.swipeRefresh?.isRefreshing = isActive
        }
    }

    private fun setupViewModelObserving() {
        viewModel.getOnFailureFlag.observe(viewLifecycleOwner) { failureFlag ->
            if (failureFlag) {
                if (childFragmentManager.fragments.size == 0) {
                    if (snackbar?.isShown == false) {
                        snackbar?.show()
                    } else if (snackbar != null) {  // Невозможно моментально показать snackbar после его скрытия, делаем паузу хотя бы 300мс
                        viewLifecycleOwner.lifecycleScope.launch  {
                            delay(SNACKBAR_RESHOW_TIMEOUT)
                            snackbar?.show()
                        }
                    }
                }
            } else {
                if (snackbar?.isShown == true) {
                    snackbar?.dismiss()
                }
            }
        }
        bindingFull.swipeRefresh.isRefreshing = true
    }

    private fun setAllTransitionAnimation() {
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val isSplashScreenEnabled = viewModel.isSplashScreenEnabled()

        val scene = Scene.getSceneForLayout(bindingBlank.root as ViewGroup, R.layout.merge_fragment_home_content, requireContext())
        if (!isFragmentClassOnceCreated && isSplashScreenEnabled) {  // запускать анимацию появления только при первой загрузке класса фрагмента
            val appBarSlideTransition = Slide(Gravity.TOP)
                .setDuration(resources.getInteger(
                    R.integer.activity_main_animations_durations_first_appearance_app_bar).toLong())
                .setInterpolator(DecelerateInterpolator())
                .addTarget(R.id.app_bar)
            val moviesRecyclerTransition = Slide(Gravity.BOTTOM)
                .setDuration(resources.getInteger(
                    R.integer.activity_main_animations_durations_first_appearance_recycler).toLong())
                .setInterpolator(DecelerateInterpolator())
                .addTarget(R.id.swipeRefresh)
            val appearingTransition = TransitionSet().apply {
                addTransition(appBarSlideTransition)
                addTransition(moviesRecyclerTransition)
                addListener(object : Transition.TransitionListener {
                    override fun onTransitionStart(transition: Transition) {}
                    override fun onTransitionCancel(transition: Transition) {}
                    override fun onTransitionPause(transition: Transition) {}
                    override fun onTransitionResume(transition: Transition) {}
                    override fun onTransitionEnd(transition: Transition) {
                        removeListener(this)
                        isFragmentClassOnceCreated = true
                    }
                })
            }
            TransitionManager.go(scene, appearingTransition)
        } else {
            if (!isFragmentClassOnceCreated) {
                isFragmentClassOnceCreated = true
            }
            scene.enter()
        }
        initializeViewsReferences(bindingBlank.root)

        _bindingFull = MergeFragmentHomeContentBinding.bind(bindingBlank.root)

        if (mainActivity?.previousFragment == WatchLaterFragment::class ||
            mainActivity?.previousFragment == FavoritesFragment::class) {
            // Включать transition анимации листания только если предыдущий фрагмент был списком фильмов
            setTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
        } else {
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            bindingFull.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.posters_appearance
            )
        }
    }


    companion object {
        var isFragmentClassOnceCreated = false
            private set

        private const val SNACKBAR_RESHOW_TIMEOUT = 400L
    }
}
