package com.sandev.moviesearcher.fragments

import android.graphics.Outline
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.*
import androidx.transition.TransitionInflater
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class DetailsFragment : Fragment() {

    companion object {
        const val TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER = 2
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
        setFloatButtonOnClick(view)
        initializeContent(view)

        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
    }

    private fun setFloatButtonOnClick(view: View) {
        val toFavoriteButton:   FloatingActionButton = view.findViewById(R.id.fab_to_favorite)
        val toWatchLaterButton: FloatingActionButton = view.findViewById(R.id.fab_to_watch_later)
        val shareButton:        FloatingActionButton = view.findViewById(R.id.fab_share)

        toFavoriteButton.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_favorite), Snackbar.LENGTH_SHORT).show()
        }
        toWatchLaterButton.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_watch_later), Snackbar.LENGTH_SHORT).show()
        }
        shareButton.setOnClickListener {
            Snackbar.make(requireContext(), view, getString(R.string.details_fragment_fab_share), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initializeContent(view: View) {
        val movie = arguments?.get(MainActivity.MOVIE_DATA_KEY) as Movie

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

    fun collapsingToolbarHasBeenExpanded(): Boolean {
        val appBar = view?.findViewById<AppBarLayout>(R.id.app_bar)
        if (appBar?.isLifted == true) {
            appBar.setExpanded(true, true)
            return true
        }
        return false
    }
}