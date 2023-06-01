package com.sandev.moviesearcher.view.fragments

import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.utils.CircularRevealAnimator
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel


abstract class MoviesListFragment : Fragment() {

    protected abstract val viewModel: MoviesListFragmentViewModel
    protected abstract var recyclerAdapter: MoviesRecyclerAdapter?

    protected var moviesDatabase: List<Movie> = emptyList()
        set(value) {
            if (value == field) return
            field = value
            initializeRecyclerAdapterList()
        }

    private var _appBar: AppBarLayout? = null
    private val appBar: AppBarLayout
        get() = _appBar!!
    private var _searchBar: SearchBar? = null
    private val searchBar: SearchBar
        get() = _searchBar!!
    private var _searchView: SearchView? = null
    private val searchView: SearchView
        get() = _searchView!!
    private var _recyclerView: RecyclerView? = null
    private val recyclerView: RecyclerView
        get() = _recyclerView!!
    private var _childFragment: FragmentContainerView? = null
    private val childFragment: FragmentContainerView
        get() = _childFragment!!

    private var circularRevealAnimator: CircularRevealAnimator? = null

    private val settingsButtonCenterCoords = IntArray(2)

    private val fragmentsTransitionDuration by lazy {
        resources.getInteger(R.integer.general_animations_durations_fragment_transition).toLong() }

    private var recyclerShapeInvalidator: RecyclerShapeInvalidator? = null
    private var recyclerShapeView: View? = null

    companion object {
        var isAppBarLifted = false

        const val SEARCH_SYMBOLS_THRESHOLD = 2
        private const val MAX_ALPHA = 255F
        private const val DIVIDER_TO_CENTER = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance()
        setSearchViewAppearance()
        setRecyclerViewAppearance(recyclerView)
        initializeToolbar()
        setupSearchBehavior()

        if (childFragmentManager.fragments.size != 0) {
            createSettingsFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Если не обнулить метод провайдера, то будет exception при обращении к resources когда фрагмент уже detached
        recyclerShapeView?.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {}
        }

        recyclerShapeView = null
        recyclerShapeInvalidator = null
        _appBar = null
        _searchBar = null
        _searchView = null
        _recyclerView = null
        _childFragment = null
        circularRevealAnimator = null
    }

    fun hideSearchView() = searchView.hide()

    fun isSearchViewHidden() = searchView.currentTransitionState == SearchView.TransitionState.HIDDEN

    protected open fun initializeRecyclerAdapterList() {
        // Загрузить в recycler результат по прошлому запросу в поиск
        searchInSearchView(viewModel.lastSearch ?: "")
    }

    protected fun initializeViewsReferences(rootView: View) {
        _appBar = rootView.findViewById(R.id.app_bar)
        _searchBar = rootView.findViewById(R.id.search_bar)
        _searchView = rootView.findViewById(R.id.search_view)
        _recyclerView = rootView.findViewById(R.id.movies_list_recycler)
        _childFragment = rootView.findViewById(R.id.settingsFragment)
    }

    protected fun setTransitionAnimation(slideGravity: Int = viewModel.lastSlideGravity, recycler: View = recyclerView) {
        viewModel.lastSlideGravity = slideGravity

        val recyclerTransition = Slide(slideGravity).apply {
            this.duration = fragmentsTransitionDuration
            interpolator = FastOutLinearInInterpolator()
            addTarget(recycler)
        }
        val transitionSet = TransitionSet().addTransition(recyclerTransition)

        if (!isAppBarLifted) {
            val appBarTransition = Fade().apply {
                this.duration = fragmentsTransitionDuration
                interpolator = AccelerateInterpolator()
                addTarget(searchBar)
            }
            transitionSet.addTransition(appBarTransition)
        }
        enterTransition = transitionSet
        returnTransition = transitionSet
        exitTransition = transitionSet
        reenterTransition = transitionSet
    }

    protected fun resetExitReenterTransitionAnimations() {
        exitTransition = null
        reenterTransition = null
    }

    private fun setupSearchBehavior() {
        val textChangeListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                searchInSearchView(text.toString())
            }
        }
        searchView.apply {
            addTransitionListener { _, previousState, newState ->
                if (previousState == SearchView.TransitionState.HIDDEN &&
                    newState == SearchView.TransitionState.SHOWING) {
                    requestFocusAndShowKeyboard()
                } else if (previousState == SearchView.TransitionState.SHOWING &&
                        newState == SearchView.TransitionState.SHOWN) {
                    editText.setText(viewModel.lastSearch)
                    editText.addTextChangedListener(textChangeListener)
                } else if (previousState == SearchView.TransitionState.SHOWN &&
                        newState == SearchView.TransitionState.HIDING) {
                    editText.removeTextChangedListener(textChangeListener)
                    // Принудительно убрать системную панель навигации на старых андроидах
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        WindowInsetsControllerCompat(requireActivity().window, requireView())
                            .hide(WindowInsetsCompat.Type.navigationBars())
                    }
                } else if (previousState == SearchView.TransitionState.HIDDEN &&
                        newState == SearchView.TransitionState.SHOWN) {
                    // Поставить слушатель при смене конфигурации
                    editText.addTextChangedListener(textChangeListener)
                }
            }
            editText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hide()
                }
                true
            }
        }
    }

    protected open fun searchInSearchView(query: String) {
        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            recyclerAdapter?.setList(viewModel.searchInDatabase(query))
        } else {
            recyclerAdapter?.setList(viewModel.moviesListLiveData.value)
        }
        viewModel.lastSearch = query
    }

    private fun createCircularRevealAnimator(view: View): CircularRevealAnimator {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)

        settingsButton.getLocationInWindow(settingsButtonCenterCoords)
        settingsButtonCenterCoords[0] = settingsButtonCenterCoords[0] + settingsButton.width / DIVIDER_TO_CENTER
        settingsButtonCenterCoords[1] = settingsButtonCenterCoords[1] + settingsButton.height / DIVIDER_TO_CENTER

        return CircularRevealAnimator(
            view.height, view.width,
            settingsButtonCenterCoords[0], settingsButtonCenterCoords[1],
            childFragment
        )
    }

    private fun createSettingsFragment() {
        requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar).let {
            it.menu.forEach { item -> item.isEnabled = false }
        }
        searchBar.menu.forEach { menuItem -> menuItem.isEnabled = false }

        if (childFragmentManager.fragments.size == 0) {
            circularRevealAnimator ?: createCircularRevealAnimator(requireView()).also { circularRevealAnimator = it }

            childFragmentManager.beginTransaction()
                .add(R.id.settingsFragment, SettingsFragment())
                .addToBackStack(null)
                .commit()

            circularRevealAnimator?.revealView()
        } else {  // Возвращение фрагмента после смены конфигурации
            childFragment.visibility = View.VISIBLE
        }

        appBar.postDelayed(circularRevealAnimator?.animationDuration ?: 0) {
            appBar.visibility = View.GONE
            searchView.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    fun destroySettingsFragment() {
        if (childFragmentManager.fragments[0] is SettingsFragment) {
            appBar.visibility = View.VISIBLE
            searchView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE

            circularRevealAnimator ?: createCircularRevealAnimator(requireView()).also { circularRevealAnimator = it }
            circularRevealAnimator?.revealView()

            appBar.postDelayed(circularRevealAnimator?.animationDuration ?: 0) {
                childFragmentManager.popBackStack()

                requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar).let {
                    it.menu.forEach { item -> item.isEnabled = true }
                }
                searchBar.menu.forEach { menuItem -> menuItem.isEnabled = true }
            }
        }
    }

    private fun initializeToolbar() {
        searchBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.top_toolbar_settings_button -> {
                    createSettingsFragment()
                    true
                }
                else -> false
            }
        }
    }

    private fun setAppBarAppearance() {
        appBar.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                insets
            }
            doOnLayout {
                outlineProvider = object : ViewOutlineProvider() {
                    val originHeight = height - paddingTop.toFloat()

                    override fun getOutline(view: View?, outline: Outline?) {
                        val deltaHeight = (originHeight + view!!.top) / originHeight
                        outline?.setRoundRect(
                            0, -resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large),
                            view.width, view.height,
                            resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large)
                                .toFloat() * deltaHeight  // Чем больше раскрыт app bar, тем больше скругление углов
                        )
                        outline?.alpha = 0f
                    }
                }
                clipToOutline = true
            }
            // Задаём цвет переднего плана для дальнейшего перекрытия им app bar при сворачивании
            foreground = ColorDrawable(context.getColor(R.color.md_theme_secondaryContainer))
            foreground.alpha = 0

            setExpanded(!isAppBarLifted, false)

            doOnLayout {
                // Слушатель смещения app bar закрашивающий search bar и обновляющий размеры recycler
                addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                    val expandedOffset = height - paddingTop.toFloat()
                    val halfExpandedOffset = (height - paddingTop) / 2

                    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                        isLiftOnScroll = _searchView?.isShowing != true // Не убирать app bar при открытом поиске

                        foreground.alpha = (LinearOutSlowInInterpolator()
                            .getInterpolation(-verticalOffset / expandedOffset) * MAX_ALPHA).toInt()
                        if (-verticalOffset > halfExpandedOffset) {
                            if (!isAppBarLifted) {
                                isAppBarLifted = true
                                setTransitionAnimation()
                            }
                        } else {
                            if (isAppBarLifted) {
                                isAppBarLifted = false
                                setTransitionAnimation()
                            }
                        }
                        recyclerShapeInvalidator?.invalidateShape()
                    }
                })
            }
        }
    }

    private fun setSearchViewAppearance() {
        searchView.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updateLayoutParams {
                    height = resources.getDimensionPixelSize(R.dimen.activity_main_search_view_height) +
                            insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
                }
                insets
            }
            doOnLayout {
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        outline?.setRoundRect(
                            0, -resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large),
                            view!!.width, view.height,
                            resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large)
                                .toFloat()
                        )
                        outline?.alpha = 0f
                    }
                }
                clipToOutline = true
            }
        }
    }

    protected open fun setRecyclerViewAppearance(view: View) {
        view.doOnPreDraw {
            recyclerShapeView = view
            recyclerShapeInvalidator = RecyclerShapeInvalidator(view)

            it.outlineProvider = object : ViewOutlineProvider() {
                val bottomNavigation =
                    requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar)
                val margin = resources.getDimensionPixelSize(R.dimen.activity_main_movies_recycler_margin_vertical)
                val freeSpace = bottomNavigation.top - appBar.height - margin - margin

                override fun getOutline(view: View?, outline: Outline?) {
                    // Прямо в методе закругления краёв обновляем высоту recycler, всё равно этот метод
                    // вызывается при каждом изменении размеров вьюхи
                    view?.updateLayoutParams {
                        if (searchView.isShowing) {
                            it.top = searchView.height + margin
                            it.bottom = bottomNavigation.top - margin
                        } else {
                            height = freeSpace - appBar.top
                        }
                    }
                    outline?.setRoundRect(
                        0, 0, view?.width ?: 0, view?.height ?: 0,
                        resources.getDimensionPixelSize(R.dimen.general_corner_radius_large)
                            .toFloat()
                    )
                    outline?.alpha = 0f
                }
            }
            it.clipToOutline = true
        }
    }

    private class RecyclerShapeInvalidator(private val view: View) {
        fun invalidateShape() = view.invalidateOutline()
    }
}