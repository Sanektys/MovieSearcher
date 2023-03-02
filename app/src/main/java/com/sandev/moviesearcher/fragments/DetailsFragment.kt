package com.sandev.moviesearcher.fragments

import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
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

    companion object {
        private const val TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_details, container, false)

        setToolbarAppearance(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeContent(view)
        setToolbarBackButton(view)
        setFloatButtonOnClick(view)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)

        activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
            animate()  // Убрать нижний navigation view
                .translationY(height.toFloat())
                .setDuration(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong())
                .withStartAction { menu.forEach { it.isEnabled = false } }
                .withEndAction { visibility = GONE }
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
            animate()
                .translationY(0f)
                .setDuration(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong())
                .withStartAction { visibility = VISIBLE; menu.forEach { it.isEnabled = true } }
                .start()
        }
        // Принятие решения о добавлении/удалении фильма в избранном
        changeFavoriteMoviesList()
        parentFragmentManager.setFragmentResult(FavoritesFragment.DETAILS_RESULT_KEY,
            bundleOf(FavoritesFragment.MOVIE_NOW_NOT_FAVORITE_KEY to !movie.isFavorite))
    }

    private fun setFloatButtonOnClick(view: View) {
        val toFavoriteButton:   FloatingActionButton = view.findViewById(R.id.fab_to_favorite)
        val toWatchLaterButton: FloatingActionButton = view.findViewById(R.id.fab_to_watch_later)
        val shareButton:        FloatingActionButton = view.findViewById(R.id.fab_share)

        if (movie.isFavorite) {
            toFavoriteButton.isSelected = true
            toFavoriteButton.setImageResource(R.drawable.favorite_icon_selected)
        }

        toFavoriteButton.setOnClickListener {
            toFavoriteButton.isSelected = !toFavoriteButton.isSelected
            if (toFavoriteButton.isSelected) {
                toFavoriteButton.setImageResource(R.drawable.favorite_icon_selected)
            } else {
                toFavoriteButton.setImageResource(R.drawable.favorite_icon_unselected)
            }
            Snackbar.make(requireContext(), view,
                if (toFavoriteButton.isSelected) getString(R.string.details_fragment_fab_add_favorite)
                else getString(R.string.details_fragment_fab_remove_favorite),
                Snackbar.LENGTH_SHORT).show()
        }
        toWatchLaterButton.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_watch_later), Snackbar.LENGTH_SHORT).show()
        }
        shareButton.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_share), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initializeContent(view: View) {
        movie = arguments?.get(MainActivity.MOVIE_DATA_KEY) as Movie

        view.findViewById<AppCompatImageView>(R.id.collapsing_toolbar_image).apply {
            setImageResource(movie.poster)
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
                view.findViewById<AppBarLayout>(R.id.app_bar).apply {
                    updateLayoutParams { height = appBarHeight + topInset }

                    // Также делаем закругление краёв снизу для тулбара
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(toolbar: View?, outline: Outline?) {
                            outline?.setRoundRect(0, -MainActivity.APP_BARS_CORNER_RADIUS.toInt(),
                                toolbar!!.width, toolbar.height,
                                MainActivity.APP_BARS_CORNER_RADIUS)
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
            val appBar: AppBarLayout = view.findViewById(R.id.app_bar)

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

    fun collapsingToolbarHasBeenExpanded(): Boolean {
        val appBar = view?.findViewById<AppBarLayout>(R.id.app_bar)
        if (appBar?.isLifted == true) {
            appBar.setExpanded(true, true)
            return true
        }
        return false
    }

    private fun changeFavoriteMoviesList() {
        val toFavoriteButton = requireView().findViewById<FloatingActionButton>(R.id.fab_to_favorite)
        if (!movie.isFavorite && toFavoriteButton.isSelected) {
            movie.isFavorite = true
            favoriteMovies.add(movie)
        } else if (movie.isFavorite && !toFavoriteButton.isSelected) {
            movie.isFavorite = false
            favoriteMovies.remove(movie)
        }
    }
}