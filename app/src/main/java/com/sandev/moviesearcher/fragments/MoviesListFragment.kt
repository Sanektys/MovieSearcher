package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.favoriteMovies
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData


abstract class MoviesListFragment : Fragment() {

    protected abstract var lastSearch: CharSequence?

    companion object {
        var isAppBarLifted = false

        private const val MAX_ALPHA = 255F
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance(view)
        setSearchViewAppearance(view)
        setRecyclerViewAppearance(view)
        initializeToolbar(view)
    }

    protected abstract fun setDefaultTransitionAnimation(view: View)

    protected fun setupSearchBehavior(moviesRecyclerAdapter: MoviesRecyclerAdapter?) {
        val textChangeListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s!!.length >= 2) {
                    var result: List<Movie>? = null
                    if (this@MoviesListFragment is HomeFragment) {
                        result = setMockData()
                    } else if (this@MoviesListFragment is FavoritesFragment) {
                        result = favoriteMovies
                    }
                    result = result!!.filter {
                        it.title!!.lowercase().contains(s.toString().lowercase())
                    }
                    moviesRecyclerAdapter?.setList(result)
                } else if (s.isEmpty()) {
                    if (this@MoviesListFragment is HomeFragment) {
                        moviesRecyclerAdapter?.setList(setMockData())
                    } else if (this@MoviesListFragment is FavoritesFragment) {
                        moviesRecyclerAdapter?.setList(favoriteMovies)
                    }
                }
                lastSearch = s
            }
        }
        requireView().findViewById<SearchView>(R.id.search_view).apply {
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
                    clearFocusAndHideKeyboard()
                    hide()
                }
                true
            }
        }
    }

    private fun initializeToolbar(view: View) {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        val searchBar: SearchBar = view.findViewById(R.id.search_bar)
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
        rootView.findViewById<AppBarLayout>(R.id.app_bar).apply {
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

            val recycler = rootView.findViewById<RecyclerView>(R.id.movies_list_recycler)
            val searchView: SearchView = rootView.findViewById(R.id.search_view)
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
                                setDefaultTransitionAnimation(rootView)
                            }
                        } else {
                            if (isAppBarLifted) {
                                isAppBarLifted = false
                                setDefaultTransitionAnimation(rootView)
                            }
                        }

                        recycler.invalidateOutline()
                    }
                })
            }
        }
    }

    private fun setSearchViewAppearance(rootView: View) {
        rootView.findViewById<SearchView>(R.id.search_view).apply {
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

    private fun setRecyclerViewAppearance(rootView: View) {
        rootView.findViewById<RecyclerView>(R.id.movies_list_recycler).apply {
            val appBar: AppBarLayout = rootView.findViewById(R.id.app_bar)
            val searchView: SearchView = rootView.findViewById(R.id.search_view)
            doOnPreDraw {
                outlineProvider = object : ViewOutlineProvider() {
                    val bottomNavigation =
                        requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar)
                    val margin = resources.getDimensionPixelSize(R.dimen.activity_main_movies_recycler_margin_vertical)
                    val freeSpace = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        bottomNavigation.top - appBar.height - margin - margin
                    } else {
                        height
                    }

                    override fun getOutline(view: View?, outline: Outline?) {
                        // Прямо в методе закругления краёв обновляем высоту recycler, всё равно этот метод
                        // вызывается при каждом изменении размеров вьюхи
                        view!!.updateLayoutParams {
                            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                                if (searchView.isShowing) {
                                    top = searchView.height + margin
                                    bottom = bottomNavigation.top - margin
                                } else {
                                    height = freeSpace - appBar.top
                                }
                            } else {
                                if (searchView.isShowing) {
                                    top = searchView.height + margin
                                    bottom = searchView.height + margin + height
                                }
                            }
                        }
                        outline?.setRoundRect(
                            0, 0, view.width, view.height,
                            resources.getDimensionPixelSize(R.dimen.general_corner_radius_large)
                                .toFloat()
                        )
                    }
                }
                clipToOutline = true
            }
        }
    }
}