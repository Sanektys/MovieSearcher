package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.*
import androidx.fragment.app.Fragment
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
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


abstract class MoviesListFragment : Fragment() {

    protected abstract var lastSearch: CharSequence?

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

    private var lastSlideGravity = Gravity.TOP
    private val fragmentsTransitionDuration by lazy {
        resources.getInteger(R.integer.general_animations_durations_fragment_transition).toLong() }

    companion object {
        var isAppBarLifted = false

        private const val MAX_ALPHA = 255F
        private const val SEARCH_SYMBOLS_THRESHOLD = 2
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance()
        setSearchViewAppearance()
        setRecyclerViewAppearance()
        initializeToolbar(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Если не обнулить метод провайдера, то будет exception при обращении к resources когда фрагмент уже detached
        recyclerView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {}
        }

        _appBar = null
        _searchBar = null
        _searchView = null
        _recyclerView = null
    }

    fun hideSearchView() = searchView.hide()

    fun isSearchViewHidden() = searchView.currentTransitionState == SearchView.TransitionState.HIDDEN

    protected fun initializeViewsReferences(rootView: View) {
        _appBar = rootView.findViewById(R.id.app_bar)
        _searchBar = rootView.findViewById(R.id.search_bar)
        _searchView = rootView.findViewById(R.id.search_view)
        _recyclerView = rootView.findViewById(R.id.movies_list_recycler)
    }

    protected fun searchInDatabase(query: CharSequence, source: List<Movie>, moviesRecyclerAdapter: MoviesRecyclerAdapter?) {
        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            val result = source.filter {
                it.title.lowercase().contains(query.toString().lowercase())
            }
            moviesRecyclerAdapter?.setList(result)
        } else if (query.isEmpty()) {
            moviesRecyclerAdapter?.setList(source)
        }
    }

    protected fun setTransitionAnimation(slideGravity: Int = lastSlideGravity) {
        lastSlideGravity = slideGravity

        val recyclerTransition = Slide(slideGravity).apply {
            this.duration = fragmentsTransitionDuration
            interpolator = FastOutLinearInInterpolator()
            addTarget(recyclerView)
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

    protected fun setupSearchBehavior(moviesRecyclerAdapter: MoviesRecyclerAdapter?, source: List<Movie>) {
        val textChangeListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchInDatabase(s, source, moviesRecyclerAdapter)
                lastSearch = s
            }
        }
        searchView.apply {
            addTransitionListener { _, previousState, newState ->
                if (previousState == SearchView.TransitionState.HIDDEN &&
                    newState == SearchView.TransitionState.SHOWING) {
                    requestFocusAndShowKeyboard()
                } else if (previousState == SearchView.TransitionState.SHOWING &&
                        newState == SearchView.TransitionState.SHOWN) {
                    editText.setText(lastSearch)
                    editText.addTextChangedListener(textChangeListener)
                } else if (previousState == SearchView.TransitionState.SHOWN &&
                        newState == SearchView.TransitionState.HIDING) {
                    editText.removeTextChangedListener(textChangeListener)
                    // Принудительно убрать системную панель навигации на старых андроидах
                    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        WindowInsetsControllerCompat(requireActivity().window, requireView())
                            .hide(WindowInsetsCompat.Type.navigationBars())
                    }
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

    private fun initializeToolbar(view: View) {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        searchBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(requireContext(), R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
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
                        isLiftOnScroll = !searchView.isShowing  // Не убирать app bar при открытом поиске

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

                        recyclerView.invalidateOutline()
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

    private fun setRecyclerViewAppearance() {
        recyclerView.doOnPreDraw {
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
}