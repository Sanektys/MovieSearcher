package com.sandev.moviesearcher

import android.content.res.Configuration
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sandev.moviesearcher.fragments.DetailsFragment
import com.sandev.moviesearcher.fragments.FavoritesFragment
import com.sandev.moviesearcher.fragments.HomeFragment
import com.sandev.moviesearcher.fragments.SplashScreenFragment
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class MainActivity : AppCompatActivity() {

    var previousFragmentName: String? = null

    private var backPressedLastTime: Long = 0

    private var homeFragment: HomeFragment? = null
    private var favoritesFragment: FavoritesFragment? = null
    private var watchLaterFragment: WatchLaterFragment? = null

    private lateinit var bottomNavigation: BottomNavigationView

    companion object {
        const val MOVIE_DATA_KEY = "MOVIE"
        const val POSTER_TRANSITION_KEY = "POSTER_TRANSITION"

        private const val HOME_FRAGMENT_COMMIT = "HOME_FRAGMENT_COMMIT"
        private const val FAVORITES_FRAGMENT_COMMIT = "FAVORITES_FRAGMENT_COMMIT"
        private const val WATCH_LATER_FRAGMENT_COMMIT = "WATCH_LATER_FRAGMENT_COMMIT"

        private const val BACK_DOUBLE_TAP_THRESHOLD = 1500L
        private const val ONE_FRAGMENT_IN_STACK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.navigation_bar)

        setSystemBarsAppearanceAndBehavior()
        setNavigationBarAppearance()
        setOnBackPressedAction()
        menuButtonsInitial()

        if (supportFragmentManager.backStackEntryCount == 0) {
            startSplashScreen()
        }
    }

    fun startHomeFragment() {
        if (homeFragment == null) {
            bottomNavigation.animate()
                .setDuration(
                    resources.getInteger(
                        R.integer.activity_main_animations_durations_first_appearance_navigation_bar
                    ).toLong()
                )
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { bottomNavigation.menu.forEach { it.isEnabled = true } }
                .start()

            homeFragment = HomeFragment()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, homeFragment!!)
                .addToBackStack(HOME_FRAGMENT_COMMIT)
                .commit()
        }
    }

    fun removeSplashScreen(splashScreenFragment: SplashScreenFragment) {
        supportFragmentManager
            .beginTransaction()
            .remove(splashScreenFragment)
            .commit()
    }

    private fun startSplashScreen() {
        if (!SplashScreenFragment.isSplashWasCreated) {
            bottomNavigation.doOnLayout { it.translationY = it.height.toFloat() }
            bottomNavigation.menu.forEach { it.isEnabled = false }

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, SplashScreenFragment())
                .commit()
        } else {
            startHomeFragment()
        }
    }

    private fun menuButtonsInitial() {
        bottomNavigation.apply {
            setOnItemSelectedListener { menuItem ->
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                when (menuItem.itemId) {
                    R.id.bottom_navigation_all_movies_button -> {
                        if (lastFragmentInBackStack != homeFragment) {
                            previousFragmentName = lastFragmentInBackStack::class.qualifiedName
                            supportFragmentManager.popBackStack(HOME_FRAGMENT_COMMIT, 0)
                        }
                        true
                    }
                    R.id.bottom_navigation_watch_later_button -> {
                        if (watchLaterFragment == null) {
                            watchLaterFragment = WatchLaterFragment()
                        }
                        startFragmentFromNavigation(watchLaterFragment!!, WATCH_LATER_FRAGMENT_COMMIT)
                        true
                    }
                    R.id.bottom_navigation_favorites_button -> {
                        if (favoritesFragment == null) {
                            favoritesFragment = FavoritesFragment()
                        }
                        startFragmentFromNavigation(favoritesFragment!!, FAVORITES_FRAGMENT_COMMIT)
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
                    }
                    is WatchLaterFragment -> {
                        menu.findItem(R.id.bottom_navigation_watch_later_button).isChecked = true
                    }
                    is FavoritesFragment -> {
                        menu.findItem(R.id.bottom_navigation_favorites_button).isChecked = true
                    }
                }
            }
        }
    }

    private fun startFragmentFromNavigation(fragment: Fragment, commitName: String) {
        val lastFragmentInBackStack = supportFragmentManager.fragments.last()
        if (lastFragmentInBackStack != fragment) {
            previousFragmentName = lastFragmentInBackStack::class.qualifiedName
            var fragmentAlreadyExists = false
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                if (supportFragmentManager.getBackStackEntryAt(i).name == commitName) {
                    fragmentAlreadyExists = true
                }
            }
            if (!fragmentAlreadyExists) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(commitName)
                    .commit()
            } else {
                supportFragmentManager.popBackStack(commitName, 0)
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
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                if (supportFragmentManager.backStackEntryCount <= ONE_FRAGMENT_IN_STACK) {
                    if (backPressedLastTime + BACK_DOUBLE_TAP_THRESHOLD >= backPressedTime) {
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity,
                            R.string.activity_main_press_back_for_exit_warning, Toast.LENGTH_SHORT).show()
                    }
                    backPressedLastTime = backPressedTime
                } else if (lastFragmentInBackStack is DetailsFragment) {
                    if (lastFragmentInBackStack.collapsingToolbarExpanded()) {
                        previousFragmentName = lastFragmentInBackStack::class.qualifiedName
                        supportFragmentManager.popBackStack()
                    }
                } else {
                    previousFragmentName = lastFragmentInBackStack::class.qualifiedName
                    supportFragmentManager.popBackStack()
                }
            }
        })
    }

    private fun setNavigationBarAppearance() {
        bottomNavigation.apply {
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