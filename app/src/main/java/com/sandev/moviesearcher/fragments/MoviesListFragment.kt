package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
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

    protected lateinit var appBar: AppBarLayout
    protected lateinit var searchBar: SearchBar
    protected lateinit var searchView: SearchView
    protected lateinit var recyclerView: RecyclerView

    private var lastSlideGravity = Gravity.TOP

    companion object {
        var isAppBarLifted = false

        private const val MAX_ALPHA = 255F
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance(view)
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
    }

    fun hideSearchView() = searchView.hide()

    fun isSearchViewHidden() = searchView.currentTransitionState == SearchView.TransitionState.HIDDEN

    protected fun initializeViewsReferences(rootView: View) {
        appBar = rootView.findViewById(R.id.app_bar)
        searchBar = rootView.findViewById(R.id.search_bar)
        searchView = rootView.findViewById(R.id.search_view)
        recyclerView = rootView.findViewById(R.id.movies_list_recycler)
    }

    protected fun setTransitionAnimation(slideGravity: Int = lastSlideGravity) {
        lastSlideGravity = slideGravity
        val duration = resources.getInteger(R.integer.general_animations_durations_fragment_transition).toLong()

        val recyclerTransition = Slide(slideGravity).apply {
            this.duration = duration
            interpolator = FastOutLinearInInterpolator()
            addTarget(recyclerView)
        }
        val transitionSet = TransitionSet().addTransition(recyclerTransition)

        if (!isAppBarLifted) {
            val appBarTransition = Fade().apply {
                this.duration = duration
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
                if (s.length >= 2) {
                    val result = source.filter {
                        it.title.lowercase().contains(s.toString().lowercase())
                    }
                    moviesRecyclerAdapter?.setList(result)
                } else if (s.isEmpty()) {
                    moviesRecyclerAdapter?.setList(source)
                }
                lastSearch = s
            }
        }
        searchView.apply {
            editText.text = lastSearch as? Editable
            editText.addTextChangedListener(textChangeListener)
            addTransitionListener { _, previousState, newState ->
                if (previousState == SearchView.TransitionState.SHOWING &&
                        newState == SearchView.TransitionState.SHOWN) {
                    editText.text = lastSearch as? Editable
                    editText.addTextChangedListener(textChangeListener)
                // При скрытии и начале открытия search view удалять обработчик для избежания
                // промежуточных загрузок полной базы при временно пустом поле при анимации
                } else if (previousState == SearchView.TransitionState.SHOWN &&
                        newState == SearchView.TransitionState.HIDING) {
                    editText.removeTextChangedListener(textChangeListener)
                } else if (previousState == SearchView.TransitionState.HIDDEN &&
                        newState == SearchView.TransitionState.SHOWING) {
                    editText.removeTextChangedListener(textChangeListener)
                    requestFocusAndShowKeyboard()
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

    private fun setAppBarAppearance(rootView: View) {
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
                    }
                }
                clipToOutline = true
            }
            // Задаём цвет переднего плана для дальнейшего перекрытия им app bar при сворачивании
            foreground = ColorDrawable(context.getColor(R.color.md_theme_secondaryContainer))
            foreground.alpha = 0

            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setExpanded(true, false)
            } else {
                setExpanded(!isAppBarLifted, false)
            }

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
                val freeSpace = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    bottomNavigation.top - appBar.height - margin - margin
                } else {
                    it.height
                }

                override fun getOutline(view: View?, outline: Outline?) {
                    // Прямо в методе закругления краёв обновляем высоту recycler, всё равно этот метод
                    // вызывается при каждом изменении размеров вьюхи
                    view?.updateLayoutParams {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            if (searchView.isShowing) {
                                it.top = searchView.height + margin
                                it.bottom = bottomNavigation.top - margin
                            } else {
                                height = freeSpace - appBar.top
                            }
                        } else {
                            if (searchView.isShowing) {
                                it.top = searchView.height + margin
                                it.bottom = searchView.height + margin + height
                            }
                        }
                    }
                    outline?.setRoundRect(
                        0, 0, view?.width ?: 0, view?.height ?: 0,
                        resources.getDimensionPixelSize(R.dimen.general_corner_radius_large)
                            .toFloat()
                    )
                }
            }
            it.clipToOutline = true
        }
    }
}