package com.sandev.moviesearcher.view.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
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
import java.util.concurrent.Executors


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
    private var moviesRecyclerManager: RecyclerView.LayoutManager? = null

    private var snackbar: Snackbar? = null

    private val backStackChangedListener: OnBackStackChangedListener
    private var sharedPreferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    companion object {
        var isFragmentClassOnceCreated = false
            private set

        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"

        private const val SNACKBAR_RESHOW_TIMEOUT = 400L
    }

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
        setupRecyclerUpdateOnScroll()
        moviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            MOVIES_RECYCLER_VIEW_STATE
        ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager?.onSaveInstanceState())
    }

    override fun onDestroyView() {
        super.onDestroyView()

        bindingFull.moviesListRecycler.clearOnChildAttachStateChangeListeners()
        childFragmentManager.removeOnBackStackChangedListener(backStackChangedListener)

        _bindingFull = null
        _bindingBlank = null
        moviesRecyclerManager = null
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
            override fun onClick(movie: Movie, posterView: ImageView) {
                resetExitReenterTransitionAnimations()
                mainActivity?.startDetailsFragment(movie, posterView)
            }
        })

        bindingFull.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = viewModel.recyclerAdapter

            moviesRecyclerManager = layoutManager!!

            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setupRecyclerUpdateOnScroll() {
        bindingFull.moviesListRecycler.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val itemPosition = bindingFull.moviesListRecycler.getChildAdapterPosition(view)

                if (itemPosition > viewModel.lastVisibleMovieCard) {
                    val itemsRemainingInList = (bindingFull.moviesListRecycler.adapter?.itemCount?.minus(1) ?: 0) - itemPosition

                    viewModel.startLoadingOnScroll(
                        lastVisibleItemPosition = itemPosition,
                        itemsRemainingInList = itemsRemainingInList,
                        screenOrientation = resources.configuration.orientation
                    )
                }
            }
            override fun onChildViewDetachedFromWindow(view: View) {}
        })
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
        viewModel.onFailureFlagLiveData.observe(viewLifecycleOwner) { failureFlag ->
            bindingFull.swipeRefresh.isRefreshing = false

            if (failureFlag) {
                if (childFragmentManager.fragments.size == 0) {
                    if (snackbar?.isShown == false) {
                        snackbar?.show()
                    } else if (snackbar != null) {  // Невозможно моментально показать snackbar после его скрытия, делаем паузу хотя бы 300мс
                        Executors.newSingleThreadExecutor().execute {
                            Thread.sleep(SNACKBAR_RESHOW_TIMEOUT)
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
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val scene = Scene.getSceneForLayout(bindingBlank.root as ViewGroup, R.layout.merge_fragment_home_content, requireContext())
        if (!isFragmentClassOnceCreated) {  // запускать анимацию появления только при первой загрузке класса фрагмента
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
            scene.enter()
        }
        initializeViewsReferences(bindingBlank.root)

        _bindingFull = MergeFragmentHomeContentBinding.bind(bindingBlank.root)

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Не включать transition анимации после выхода из окна деталей
            bindingFull.root.postDelayed(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong()) {
                setTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
            }
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            bindingFull.moviesListRecycler.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetExitReenterTransitionAnimations()
        } else {
            setTransitionAnimation(Gravity.START, bindingFull.swipeRefresh)
        }
    }
}