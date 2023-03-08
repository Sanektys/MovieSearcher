package com.sandev.moviesearcher

import android.content.res.Configuration
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sandev.moviesearcher.fragments.DetailsFragment
import com.sandev.moviesearcher.fragments.FavoritesFragment
import com.sandev.moviesearcher.fragments.HomeFragment
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class MainActivity : AppCompatActivity() {

    private var backPressedLastTime: Long = 0
    private var homeFragmentCommitId: Int = FRAGMENT_UNCOMMITTED
    private var favoritesFragmentCommitId: Int = FRAGMENT_UNCOMMITTED

    companion object {
        private const val HOME_FRAGMENT_COMMIT_ID_KEY = "HOME_FRAGMENT_COMMIT_KEY"
        private const val FAVORITES_FRAGMENT_COMMIT_ID_KEY = "FAVORITES_FRAGMENT_COMMIT_KEY"
        const val MOVIE_DATA_KEY = "MOVIE"
        const val POSTER_TRANSITION_KEY = "POSTER_TRANSITION"

        private const val BACK_DOUBLE_TAP_THRESHOLD = 1500L
        private const val ONE_FRAGMENT_IN_STACK = 1
        private const val FRAGMENT_UNCOMMITTED = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSystemBarsAppearanceAndBehavior()
        setNavigationBarAppearance()
        setOnBackPressedAction()
        menuButtonsInitial()

        if (supportFragmentManager.backStackEntryCount == 0) {
            homeFragmentCommitId = supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, HomeFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(HOME_FRAGMENT_COMMIT_ID_KEY, homeFragmentCommitId)
        outState.putInt(FAVORITES_FRAGMENT_COMMIT_ID_KEY, favoritesFragmentCommitId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        homeFragmentCommitId = savedInstanceState.getInt(HOME_FRAGMENT_COMMIT_ID_KEY)
        favoritesFragmentCommitId = savedInstanceState.getInt(FAVORITES_FRAGMENT_COMMIT_ID_KEY)
    }

    private fun menuButtonsInitial() {
        findViewById<BottomNavigationView>(R.id.navigation_bar).apply {
            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.bottom_navigation_all_movies_button -> {
                        supportFragmentManager.popBackStack(homeFragmentCommitId, 0)
                        true
                    }
                    R.id.bottom_navigation_watch_later_button -> {
                        Toast.makeText(context, it.title, Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.bottom_navigation_favorites_button -> {
                        if (favoritesFragmentCommitId == FRAGMENT_UNCOMMITTED) {
                            favoritesFragmentCommitId = supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment, FavoritesFragment())
                                .addToBackStack(null)
                                .commit()
                        }
                        true
                    }
                    else -> false
                }
            }
            // Navigation bar будет отслеживать backstack чтобы вовремя переключать кнопки меню
            supportFragmentManager.addOnBackStackChangedListener {
                when (supportFragmentManager.fragments.last()) {
                    is HomeFragment -> {
                        menu.findItem(R.id.bottom_navigation_all_movies_button).isChecked = true
                        favoritesFragmentCommitId = FRAGMENT_UNCOMMITTED
                    }
                    is FavoritesFragment -> {
                        menu.findItem(R.id.bottom_navigation_favorites_button).isChecked = true
                    }
                }
            }
        }
    }

    fun startDetailsFragment(movie: Movie, posterView: ImageView) {
        val bundle = Bundle()
        bundle.putParcelable(MOVIE_DATA_KEY, movie)
        val transitionName = posterView.transitionName
        bundle.putString(POSTER_TRANSITION_KEY, transitionName)

        val detailsFragment = DetailsFragment().apply {
            arguments = bundle
        }

        supportFragmentManager
            .beginTransaction()
            .addSharedElement(posterView, transitionName)
            .replace(R.id.fragment, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setSystemBarsAppearanceAndBehavior() {
        // Отменяем коллизию status & navigation bars, чтобы наши вьюхи проходили под ними
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val view: View = findViewById(R.id.navigation_bar)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Для альбомной ориентации убираем системные кнопки навигации с возможностью вытащить их по жесту
            WindowInsetsControllerCompat(window, view).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun setOnBackPressedAction() {
        onBackPressedDispatcher.addCallback(this,  object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backPressedTime = System.currentTimeMillis()
                if (supportFragmentManager.backStackEntryCount <= ONE_FRAGMENT_IN_STACK) {
                    if (backPressedLastTime + BACK_DOUBLE_TAP_THRESHOLD >= backPressedTime) {
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity,
                            R.string.activity_main_press_back_for_exit_warning, Toast.LENGTH_SHORT).show()
                    }
                    backPressedLastTime = backPressedTime
                } else if (supportFragmentManager.fragments.last() is DetailsFragment) {
                    if (!(supportFragmentManager.fragments.last() as DetailsFragment)
                            .collapsingToolbarHasBeenExpanded()) {
                        supportFragmentManager.popBackStack()
                    }
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        })
    }

    private fun setNavigationBarAppearance() {
        findViewById<BottomNavigationView>(R.id.navigation_bar).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
                updateLayoutParams {
                    height = resources.getDimensionPixelSize(R.dimen.activity_main_bottom_navigation_bar_height) + paddingBottom
                }
                insets
            }

            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, 0,
                        view!!.width, (view.height + resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large)),
                        resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large).toFloat()
                    )
                }
            }
            clipToOutline = true
        }
    }
}