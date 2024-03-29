package com.sandev.moviesearcher.view.fragments

import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.core.view.postDelayed
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentOnAttachListener
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionSet
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.utils.CircularRevealAnimator
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit


abstract class MoviesListFragment : Fragment() {

    protected abstract val viewModel: MoviesListFragmentViewModel

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

    private var searchInputObserver: Disposable? = null

    private val backStackAttachListener: FragmentOnAttachListener


    init {
        backStackAttachListener = FragmentOnAttachListener { _, fragment ->
            if (fragment !is MoviesListFragment) {
                // Отключить анимацию "листания" если открываемый фрагмент не список с фильмами
                resetExitReenterTransitionAnimations()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance()
        setSearchViewAppearance()
        setRecyclerViewAppearance(recyclerView)
        setupRecyclerUpdateOnScroll()
        initializeToolbar()
        setupSearchBehavior()

        requireActivity().supportFragmentManager.addFragmentOnAttachListener(backStackAttachListener)

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

        requireActivity().supportFragmentManager.removeFragmentOnAttachListener(backStackAttachListener)

        recyclerShapeView = null
        recyclerShapeInvalidator = null
        _appBar = null
        _searchBar = null
        _searchView = null
        _recyclerView = null
        _childFragment = null
        circularRevealAnimator = null

        searchInputObserver?.dispose()
    }

    fun hideSearchView() = searchView.hide()

    fun isSearchViewHidden() = searchView.currentTransitionState == SearchView.TransitionState.HIDDEN

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
        transitionSet.addListener(object : Transition.TransitionListener {
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}

            override fun onTransitionEnd(transition: Transition) {
                resetExitReenterTransitionAnimations()
            }
        })

        enterTransition = transitionSet
        exitTransition = transitionSet
    }

    protected fun setExitTransitionAnimation(slideGravity: Int = viewModel.lastSlideGravity, recycler: View = recyclerView) {
        val newTopFragmentInBackStack = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment)

        if (newTopFragmentInBackStack == this) return

        if (newTopFragmentInBackStack is MoviesListFragment) {
            // Подготовить анимацию листания(уход фрагмента вбок) если новый фрагмент - это список фильмов
            setTransitionAnimation(slideGravity, recycler)
        }
    }

    protected fun resetExitReenterTransitionAnimations() {
        enterTransition = null
        exitTransition = null
    }

    private fun setupRecyclerUpdateOnScroll() {
        recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                val itemPosition = recyclerView.getChildAdapterPosition(view)

                if (itemPosition >= viewModel.lastVisibleMovieCard) {
                    viewModel.startLoadingOnScroll(lastVisibleItemPosition = itemPosition,)
                }
            }
            override fun onChildViewDetachedFromWindow(view: View) {}
        })
    }

    private fun setupSearchBehavior() {
        val defaultHint = searchBar.hint?.toString()

        if (viewModel.lastSearch.isNotEmpty()) {
            searchBar.hint = viewModel.lastSearch
            searchView.hint = viewModel.lastSearch
        }

        var textChangeListener: TextWatcher? = null
        searchInputObserver = Observable.create<String> { emitter ->
            textChangeListener = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable?) {}

                override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                    if (text.isEmpty()) {
                        searchBar.hint = defaultHint
                        searchView.hint = defaultHint
                    }
                    emitter.onNext(text.toString())
                }
            }
        }
        .debounce(SEARCH_INPUT_QUERY_SUBMIT_DELAY, TimeUnit.MILLISECONDS)
        .subscribe { query ->
            viewModel.searchInSearchView(query)
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
                    // Сохранение текста на экране при выходе из ввода
                    searchBar.hint = text?.ifEmpty { defaultHint }
                    searchView.hint = text?.ifEmpty { defaultHint }

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
        searchBar.menu.forEach { menuItem -> menuItem.isEnabled = false }

        if (childFragmentManager.fragments.size == 0) {
            circularRevealAnimator ?: createCircularRevealAnimator(requireView()).also { circularRevealAnimator = it }

            childFragmentManager.beginTransaction()
                .add(R.id.settingsFragment, SettingsFragment())
                .addToBackStack(null)
                .commit()
        } else {  // Возвращение фрагмента после смены конфигурации
            childFragment.visibility = View.VISIBLE
        }

        appBar.postDelayed(circularRevealAnimator?.animationDuration ?: 0) {
            appBar.visibility = View.GONE
            searchView.visibility = View.GONE
            recyclerView.visibility = View.GONE

            // Подкрашивать уголки navigationBar под текущий цвет фона окна настроек
            requireActivity().findViewById<View>(R.id.rootBackground).apply {
                val outValue = TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, outValue, true)
                background = AppCompatResources.getDrawable(context, outValue.resourceId)
            }

            // Т.к. верхний блок уже выставил нужный цвет, то убираем его из корневого вью окна настроек в целях оптимизации переотрисовок
            requireActivity().findViewById<View>(R.id.fragmentSettingsRootView).apply {
                background = null
            }
        }
    }

    fun revealSettingsFragment() = circularRevealAnimator?.revealView()

    fun destroySettingsFragment() {
        if (childFragmentManager.fragments[0] is SettingsFragment) {
            appBar.visibility = View.VISIBLE
            searchView.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE

            requireActivity().findViewById<View>(R.id.fragmentSettingsRootView).apply {
                val outValue = TypedValue()
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceInverse, outValue, true)
                background = AppCompatResources.getDrawable(context, outValue.resourceId)
            }

            circularRevealAnimator ?: createCircularRevealAnimator(requireView()).also { circularRevealAnimator = it }
            circularRevealAnimator?.revealView()

            requireActivity().findViewById<View>(R.id.rootBackground).apply {
                background = null
            }

            appBar.postDelayed(circularRevealAnimator?.animationDuration ?: 0) {
                childFragmentManager.popBackStack()

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


    companion object {
        var isAppBarLifted = false

        private const val MAX_ALPHA = 255F
        private const val DIVIDER_TO_CENTER = 2

        private const val SEARCH_INPUT_QUERY_SUBMIT_DELAY = 500L
    }
}