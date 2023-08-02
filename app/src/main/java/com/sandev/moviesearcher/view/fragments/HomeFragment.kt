package com.sandev.moviesearcher.view.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.databinding.FragmentHomeBinding
import com.sandev.moviesearcher.databinding.MergeFragmentHomeContentBinding
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class HomeFragment : MoviesListFragment() {

    override val viewModel: HomeFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[HomeFragmentViewModel::class.java]
    }

    private var _bindingFull: MergeFragmentHomeContentBinding? = null
    private val bindingFull: MergeFragmentHomeContentBinding
        get() = _bindingFull!!
    private var _bindingBlank: FragmentHomeBinding? = null
    private val bindingBlank: FragmentHomeBinding
        get() = _bindingBlank!!

    private var mainActivity: MainActivity? = null

    private var snackbar: Snackbar? = null

    private val backStackChangedListener: OnBackStackChangedListener
    private var sharedPreferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null


    init {
        backStackChangedListener = createChildFragmentsChangeListener()
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
        prepareErrorConnectionSnackbar()

        childFragmentManager.addOnBackStackChangedListener(backStackChangedListener)
        initializeOnSharedPreferenceChangeListener()

        super.onViewCreated(view, savedInstanceState)

        setupViewModelObserving()

        initializeMovieRecycler()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        bindingFull.moviesListRecycler.clearOnChildAttachStateChangeListeners()
        childFragmentManager.removeOnBackStackChangedListener(backStackChangedListener)

        _bindingFull = null
        _bindingBlank = null
    }

    override fun onDestroy() {
        super.onDestroy()

        removeOnSharedPreferenceChangeListener()
    }

    override fun setRecyclerViewAppearance(view: View) {
        super.setRecyclerViewAppearance(bindingFull.swipeRefresh)
    }

    private fun initializeMovieRecycler() {
        viewModel.recyclerAdapter.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ShapeableImageView) {
                resetExitReenterTransitionAnimations()
                mainActivity?.startDetailsFragment(movie, posterView)
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

    private fun initializeOnSharedPreferenceChangeListener() {
        sharedPreferencesChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    SharedPreferencesProvider.KEY_CATEGORY -> {
                        _bindingFull?.swipeRefresh?.isRefreshing = true
                    }
                }
            }
        viewModel.sharedPreferencesInteractor.addSharedPreferencesChangeListener(
            sharedPreferencesChangeListener ?: return
        )
    }

    private fun removeOnSharedPreferenceChangeListener() {
        viewModel.sharedPreferencesInteractor.removeSharedPreferencesChangeListener(
            sharedPreferencesChangeListener ?: return
        )
    }

    private fun setupViewModelObserving() {
        viewModel.getOnFailureFlag.observe(viewLifecycleOwner) { failureFlag ->
            bindingFull.swipeRefresh.isRefreshing = false

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
            }
            TransitionManager.go(scene, appearingTransition)
            isFragmentClassOnceCreated = true
        } else {
            if (!isFragmentClassOnceCreated) {
                isFragmentClassOnceCreated = true
            }
            scene.enter()
        }
        initializeViewsReferences(bindingBlank.root)

        _bindingFull = MergeFragmentHomeContentBinding.bind(bindingBlank.root)

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Не включать transition анимации после выхода из окна деталей
            resetExitReenterTransitionAnimations()

            val layoutAnimationListener = object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation) {
                    bindingFull.moviesListRecycler.layoutAnimationListener = null
                    setTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
                }
            }
            bindingFull.moviesListRecycler.layoutAnimationListener = layoutAnimationListener
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            bindingFull.moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                requireContext(), R.anim.posters_appearance
            )
        } else {
            setTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
        }
    }


    companion object {
        var isFragmentClassOnceCreated = false
            private set

        private const val SNACKBAR_RESHOW_TIMEOUT = 400L
    }
}
