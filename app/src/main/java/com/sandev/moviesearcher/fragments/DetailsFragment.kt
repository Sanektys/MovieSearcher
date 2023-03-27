package com.sandev.moviesearcher.fragments

import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.favoriteMovies


class DetailsFragment : Fragment() {

    private lateinit var movie: Movie
    private var isFavoriteMovie: Boolean = false
    private var configurationChanged = false

    private lateinit var fragmentThatLaunchedDetails: String

    private lateinit var appBar: AppBarLayout
    private lateinit var fabFavorite: FloatingActionButton
    private lateinit var fabWatchLater: FloatingActionButton
    private lateinit var fabShare: FloatingActionButton

    companion object {
        private const val FRAGMENT_LAUNCHED_KEY = "FRAGMENT_LAUNCHED"
        private const val FAVORITE_BUTTON_SELECTED_KEY = "FAVORITE_BUTTON_SELECTED"

        private const val TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        appBar = rootView.findViewById(R.id.app_bar)
        fabFavorite = rootView.findViewById(R.id.fab_to_favorite)
        fabWatchLater = rootView.findViewById(R.id.fab_to_watch_later)
        fabShare = rootView.findViewById(R.id.fab_share)

        setToolbarAppearance(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentThatLaunchedDetails = savedInstanceState?.getString(FRAGMENT_LAUNCHED_KEY) ?:
                (activity as MainActivity).previousFragmentName!!

        initializeContent(view)
        setToolbarBackButton(view)
        setFloatButtonOnClick(view)
        if (savedInstanceState != null) {
            fabFavorite.isSelected = savedInstanceState.getBoolean(FAVORITE_BUTTON_SELECTED_KEY)
            fabFavorite.setImageResource(R.drawable.favorite_icon_selector)
        }

        setTransitionAnimation(view)

        activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
            animate()  // Убрать нижний navigation view
                .translationY(height.toFloat())
                .setDuration(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong())
                .withStartAction { menu.forEach { it.isEnabled = false } }
                .withEndAction { visibility = GONE }
                .start()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FRAGMENT_LAUNCHED_KEY, fragmentThatLaunchedDetails)
        outState.putBoolean(FAVORITE_BUTTON_SELECTED_KEY, fabFavorite.isSelected)
        configurationChanged = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!configurationChanged) {
            activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
                animate()
                    .translationY(0f)
                    .setDuration(
                        resources.getInteger(R.integer.activity_main_animations_durations_poster_transition)
                            .toLong()
                    )
                    .withStartAction { visibility = VISIBLE; menu.forEach { it.isEnabled = true } }
                    .start()
            }
            // Принятие решения о добавлении/удалении фильма в избранном
            changeFavoriteMoviesList()
        }
    }

    private fun setFloatButtonOnClick(view: View) {

        if (favoriteMovies.find{ it.title == movie.title } != null) {
            isFavoriteMovie = true
            fabFavorite.isSelected = true
            fabFavorite.setImageResource(R.drawable.favorite_icon_selector)
        }

        fabFavorite.setOnClickListener {
            fabFavorite.isSelected = !fabFavorite.isSelected
            if (fabFavorite.isSelected) {
                fabFavorite.setImageResource(R.drawable.favorite_icon_selector)
            } else {
                fabFavorite.setImageResource(R.drawable.favorite_icon_selector)
            }
            Snackbar.make(requireContext(), view,
                if (fabFavorite.isSelected) getString(R.string.details_fragment_fab_add_favorite)
                else getString(R.string.details_fragment_fab_remove_favorite),
                Snackbar.LENGTH_SHORT).show()
        }
        fabWatchLater.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_watch_later), Snackbar.LENGTH_SHORT).show()
        }
        fabShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.details_fragment_fab_share_sending_text,
                movie.title, movie.description))
            startActivity(Intent.createChooser(intent, getString(R.string.details_fragment_fab_share_to)))
        }
    }

    private fun initializeContent(view: View) {
        movie = arguments?.getParcelable(MainActivity.MOVIE_DATA_KEY)!!

        view.findViewById<ImageView>(R.id.collapsing_toolbar_image).apply {
            Glide.with(this@DetailsFragment).load(movie.poster).centerCrop().into(this)
            transitionName = arguments?.getString(MainActivity.POSTER_TRANSITION_KEY)
        }
        view.findViewById<TextView>(R.id.title).text = movie.title
        view.findViewById<TextView>(R.id.description).text = movie.description
    }

    private fun setToolbarAppearance(rootView: View) {
        rootView.let { view ->
            // Установка верхнего паддинга у тулбара для того, чтобы он не провалился под статус бар
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top  // Если под статус бар можно "залезть", то вернётся его высота
                val appBarHeight = resources.getDimensionPixelSize(R.dimen.activity_details_app_bar_height)
                val toolbarSize = resources.getDimensionPixelSize(R.dimen.activity_details_app_bar_toolbar_height)

                view.findViewById<MaterialToolbar>(R.id.collapsing_toolbar_toolbar).apply {
                    updatePadding(top = topInset)  // Обновляем паддинг только у свёрнутого тулбара, иначе изображение съедет
                    updateLayoutParams { height = toolbarSize + paddingTop }  // Закономерно увеличиваем высоту тулбара, чтобы его контент не скукожило
                }
                appBar.apply {
                    updateLayoutParams { height = appBarHeight + topInset }

                    // Также делаем закругление краёв снизу для тулбара
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(toolbar: View?, outline: Outline?) {
                            outline?.setRoundRect(0, -resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large),
                                toolbar!!.width, toolbar.height,
                                resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large).toFloat())
                        }
                    }
                    clipToOutline = true
                }
                view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar).apply {
                    // Обновляем позицию триггера перехода на свёрнутый тулбар, где фон заменяется на цвет
                    doOnPreDraw {
                        scrimVisibleHeightTrigger =
                            findViewById<MaterialToolbar>(R.id.collapsing_toolbar_toolbar).height *
                                    TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER
                    }
                }
                insets
            }
        }
    }

    private fun setToolbarBackButton(view: View) {
        view.findViewById<MaterialToolbar>(R.id.collapsing_toolbar_toolbar).apply {
            setNavigationIcon(R.drawable.round_arrow_back)
            setNavigationOnClickListener {
                if (appBar.isLifted) {
                    appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                            if (verticalOffset == 0) {  // collapsing toolbar полностью развёрнут
                                activity?.onBackPressedDispatcher?.onBackPressed()
                                appBar.removeOnOffsetChangedListener(this)
                            }
                        }
                    })
                    appBar.setExpanded(true, true)
                } else {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }
    }

    fun collapsingToolbarExpanded(): Boolean {
        if (appBar.isLifted) {
            appBar.setExpanded(true, true)
            return false
        }
        return true
    }

    private fun changeFavoriteMoviesList() {
        if (!isFavoriteMovie && fabFavorite.isSelected) {
            isFavoriteMovie = true
            favoriteMovies.add(movie)
        } else if (isFavoriteMovie && !fabFavorite.isSelected) {
            isFavoriteMovie = false
            favoriteMovies.remove(movie)
            if (fragmentThatLaunchedDetails == FavoritesFragment::class.qualifiedName) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    FavoritesFragment.DETAILS_RESULT_KEY,
                    bundleOf(FavoritesFragment.MOVIE_NOW_NOT_FAVORITE_KEY to true)
                )
            }
        }
    }

    private fun setTransitionAnimation(view: View) {
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)

        val movieTitle: TextView = view.findViewById(R.id.title)
        val movieDescription: TextView = view.findViewById(R.id.description)
        val duration = resources.getInteger(R.integer.activity_main_animations_durations_poster_transition)
            .toLong()

        enterTransition = TransitionSet().apply {
            val appBarTransition = Fade().apply {
                mode = Fade.MODE_IN
                interpolator = DecelerateInterpolator()
                addTarget(appBar)
            }
            val movieInformationTransition = Fade().apply {
                mode = Fade.MODE_IN
                interpolator = FastOutLinearInInterpolator()
                addTarget(movieTitle)
                addTarget(movieDescription)
            }
            val fabTransition = Slide(Gravity.END).apply {
                mode = Slide.MODE_IN
                interpolator = LinearOutSlowInInterpolator()
                addTarget(fabFavorite)
                addTarget(fabWatchLater)
                addTarget(fabShare)
            }
            this.duration = duration
            addTransition(appBarTransition)
            addTransition(movieInformationTransition)
            addTransition(fabTransition)
        }
    }
}