package com.sandev.moviesearcher.view.fragments

import android.content.SharedPreferences
import android.content.res.Configuration
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
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
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
    override var recyclerAdapter: MoviesRecyclerAdapter?
        set(value) {
            Companion.recyclerAdapter = value
        }
        get() = Companion.recyclerAdapter

    private var snackbar: Snackbar? = null

    private val backStackChangedListener: OnBackStackChangedListener
    private var sharedPreferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    companion object {
        var isFragmentClassOnceCreated = false
            private set

        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"

        private var recyclerAdapter: MoviesRecyclerAdapter? = null

        private const val RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD = 5
        private const val LOADING_THRESHOLD_MULTIPLIER_FOR_LANDSCAPE = 2
        private const val INITIAL_PAGE_IN_RECYCLER = 1

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

        if (activity?.isChangingConfigurations == false) {
            removeOnSharedPreferenceChangeListener()

            recyclerAdapter = null
        }
    }

    override fun searchInSearchView(query: String) {
        if (query == viewModel.lastSearch) return

        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            if (!viewModel.isInSearchMode) {
                viewModel.isInSearchMode = true
                recyclerAdapter?.clearList()
            }
            viewModel.getSearchedMoviesFromApi(query, INITIAL_PAGE_IN_RECYCLER)
        } else if (query.isEmpty()) {
            if (viewModel.isInSearchMode) {
                viewModel.isInSearchMode = false
                recyclerAdapter?.clearList()
            }
            viewModel.getMoviesFromApi(INITIAL_PAGE_IN_RECYCLER)
        }
        viewModel.lastSearch = query
    }

    override fun initializeRecyclerAdapterList() {
        if (_bindingFull == null) {  // Обновление адаптера когда сам фрагмент прошёл onDestroyedView
            recyclerAdapter?.clearList()
        } else if (bindingFull.swipeRefresh.isRefreshing) {
            recyclerAdapter?.clearList()
        }
        if (viewModel.isInSearchMode) {
            if (viewModel.isPaginationLoadingOnProcess) {
                // Если строка поиска не пуста (isInSearchMode = true) и происходит подгрузка
                // при скролле - добавлять новые списки в конец
                recyclerAdapter?.addMovieCards(moviesDatabase)
            } else {
                // Если в поле поиска был произведён ввод, то устанавливается новый список
                recyclerAdapter?.setList(moviesDatabase)
            }
        } else {
            // Если строка поиска пуста - просто добавлять приходящие новые списки в конец
            recyclerAdapter?.addMovieCards(moviesDatabase)
        }
        viewModel.isPaginationLoadingOnProcess = false
    }

    override fun setRecyclerViewAppearance(view: View) {
        super.setRecyclerViewAppearance(bindingFull.swipeRefresh)
    }

    private fun initializeMovieRecycler() {
        if (recyclerAdapter == null) {
            recyclerAdapter = MoviesRecyclerAdapter()
        }
        recyclerAdapter?.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ImageView) {
                resetExitReenterTransitionAnimations()
                mainActivity?.startDetailsFragment(movie, posterView)
            }
        })

        bindingFull.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = recyclerAdapter

            moviesRecyclerManager = layoutManager!!

            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setupRecyclerUpdateOnScroll() {
        bindingFull.moviesListRecycler.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                if (viewModel.onFailureFlag) return
                
                val itemPosition = bindingFull.moviesListRecycler.getChildAdapterPosition(view)

                if (itemPosition > viewModel.latestAttachedMovieCard) {
                    viewModel.latestAttachedMovieCard = itemPosition
                    val loadingThreshold =
                        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD * LOADING_THRESHOLD_MULTIPLIER_FOR_LANDSCAPE
                        } else {
                            RECYCLER_ITEMS_REMAIN_BEFORE_LOADING_THRESHOLD
                        }

                    val itemsRemainingInList = (bindingFull.moviesListRecycler.adapter?.itemCount?.minus(1) ?: 0) - itemPosition
                    if (itemsRemainingInList <= loadingThreshold
                            && !viewModel.isPaginationLoadingOnProcess
                            && viewModel.isNextPageCanBeDownloaded()) {
                        viewModel.isPaginationLoadingOnProcess = true
                        if (viewModel.isInSearchMode) {
                            viewModel.getSearchedMoviesFromApi()
                        } else {
                            viewModel.getMoviesFromApi()
                        }
                    }
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
                viewModel.isOffline = false
                refreshMoviesList()
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
            viewModel.isOffline = false
            refreshMoviesList()
        }
    }

    private fun initializeOnSharedPreferenceChangeListener() {
        sharedPreferencesChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    App.instance.getString(R.string.shared_preferences_settings_key_category) -> {
                        _bindingFull?.swipeRefresh?.isRefreshing = true
                        refreshMoviesList()
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

    private fun refreshMoviesList() {
        moviesDatabase = emptyList()
        viewModel.clearCachedList()
        if (viewModel.isInSearchMode) {
            viewModel.getSearchedMoviesFromApi(page = INITIAL_PAGE_IN_RECYCLER)
        } else {
            viewModel.getMoviesFromApi(page = INITIAL_PAGE_IN_RECYCLER)
        }
    }

    private fun setupViewModelObserving() {
        viewModel.moviesListLiveData.observe(viewLifecycleOwner) { database ->
            moviesDatabase = database
        }
        viewModel.onFailureFlagLiveData.observe(viewLifecycleOwner) { failureFlag ->
            if (bindingFull.swipeRefresh.isRefreshing) {
                bindingFull.swipeRefresh.isRefreshing = false
            }
            if (failureFlag) {
                viewModel.isPaginationLoadingOnProcess = false

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