package com.sandev.moviesearcher.view

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Outline
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.databinding.ActivityMainBinding
import com.sandev.moviesearcher.view.fragments.DetailsFragment
import com.sandev.moviesearcher.view.fragments.FavoritesFragment
import com.sandev.moviesearcher.view.fragments.HomeFragment
import com.sandev.moviesearcher.view.fragments.MoviesListFragment
import com.sandev.moviesearcher.view.fragments.SplashScreenFragment
import com.sandev.moviesearcher.view.fragments.WatchLaterFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    var previousFragmentName: String? = null

    private var backPressedLastTime: Long = 0

    private lateinit var binding: ActivityMainBinding

    private var homeFragment = HomeFragment()
    private var favoritesFragment = FavoritesFragment()
    private var watchLaterFragment = WatchLaterFragment()

    private val dummyOnBackPressed = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSystemBarsAppearanceAndBehavior()
        setNavigationBarAppearance()
        setOnBackPressedAction()
        menuButtonsInitial()

        if (supportFragmentManager.backStackEntryCount == 0) {
            startSplashScreen()
        }
    }

    fun startHomeFragment() {
        if (!HomeFragment.isFragmentClassOnceCreated) {
            binding.navigationBar.run {
                animate()
                .setDuration(
                    resources.getInteger(
                        R.integer.activity_main_animations_durations_first_appearance_navigation_bar
                    ).toLong()
                )
                .translationY(0f)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction { binding.navigationBar.menu.forEach { it.isEnabled = true } }
                .start()
            }
        }
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment, homeFragment)
            .addToBackStack(HOME_FRAGMENT_COMMIT)
            .commit()
    }

    fun removeSplashScreen(splashScreenFragment: SplashScreenFragment) {
        supportFragmentManager
            .beginTransaction()
            .remove(splashScreenFragment)
            .commit()
    }

    private fun startSplashScreen() {
        if (!SplashScreenFragment.isSplashWasCreated) {
            binding.navigationBar.doOnLayout { it.translationY = it.height.toFloat() }
            binding.navigationBar.menu.forEach { it.isEnabled = false }

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment, SplashScreenFragment())
                .commit()
        } else {
            startHomeFragment()
        }
    }

    private fun menuButtonsInitial() {
        binding.navigationBar.apply {
            setOnItemSelectedListener { menuItem ->
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                if (lastFragmentInBackStack is MoviesListFragment) {
                    if (lastFragmentInBackStack.isSearchViewHidden()) {
                        navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                        true
                    } else {
                        lastFragmentInBackStack.hideSearchView()
                        lifecycleScope.launch {
                            while (true) {
                                if (lastFragmentInBackStack.isSearchViewHidden()) break
                                delay(LOOP_CYCLE_DELAY)
                            }
                            navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                        }
                        false
                    }
                } else {
                    navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                    true
                }
            }
            // Navigation bar будет отслеживать backstack чтобы вовремя переключать кнопки меню
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.fragments.isNotEmpty()) {
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
                } else {
                    menu.forEach { it.isChecked = false }
                }
            }
        }
    }

    private fun navigationMenuItemClick(lastFragmentInBackStack: Fragment, menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.bottom_navigation_all_movies_button -> {
                if (lastFragmentInBackStack !is HomeFragment) {
                    if (lastFragmentInBackStack is WatchLaterFragment) {
                        lastFragmentInBackStack.prepareTransitionBeforeNewFragment(true)
                    }
                    supportFragmentManager.popBackStackWithSavingFragments(HOME_FRAGMENT_COMMIT)
                }
            }
            R.id.bottom_navigation_watch_later_button -> {
                startFragmentFromNavigation(watchLaterFragment, WATCH_LATER_FRAGMENT_COMMIT)
            }
            R.id.bottom_navigation_favorites_button -> {
                if (lastFragmentInBackStack is WatchLaterFragment) {
                    lastFragmentInBackStack.prepareTransitionBeforeNewFragment(false)
                }
                startFragmentFromNavigation(favoritesFragment, FAVORITES_FRAGMENT_COMMIT)
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
                supportFragmentManager.popBackStackWithSavingFragments(commitName)
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

        previousFragmentName = supportFragmentManager.fragments.last()::class.qualifiedName
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

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Для альбомной ориентации убираем системные кнопки навигации с возможностью вытащить их по жесту
            WindowInsetsControllerCompat(window, binding.root).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun FragmentManager.popBackStackWithSavingFragments(commitName: String = "") {
        fun saveAndPop() {
            val lastFragmentInBackStack = fragments.last()
            when (lastFragmentInBackStack) {
                is HomeFragment -> homeFragment       = lastFragmentInBackStack
                is WatchLaterFragment -> watchLaterFragment = lastFragmentInBackStack
                is FavoritesFragment -> favoritesFragment  = lastFragmentInBackStack
            }
            previousFragmentName = lastFragmentInBackStack::class.qualifiedName
            popBackStack()
        }

        if (commitName.isNotEmpty()) {
            for (i in (0 until backStackEntryCount).reversed()) {
                if (getBackStackEntryAt(i).name == commitName) return
                saveAndPop()
            }
        } else {
            saveAndPop()
        }
    }

    private fun setOnBackPressedAction() {
        onBackPressedDispatcher.addCallback(this, dummyOnBackPressed)
        onBackPressedDispatcher.addCallback(this,  object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                if (lastFragmentInBackStack is MoviesListFragment) {
                    if (!lastFragmentInBackStack.isSearchViewHidden()) {
                        // Т.к. searchView не убирается сразу, то нужно ждать пока оно закроется
                        lastFragmentInBackStack.hideSearchView()
                        lifecycleScope.launch {
                            while (true) {
                                if (lastFragmentInBackStack.isSearchViewHidden()) break
                                delay(LOOP_CYCLE_DELAY)
                            }
                            isEnabled = true
                            dummyOnBackPressed.isEnabled = false
                            runOnUiThread {
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        // На время закрытия searchView не обрабатывать клики
                        dummyOnBackPressed.isEnabled = true
                        isEnabled = false
                        return
                    }

                    if (lastFragmentInBackStack is WatchLaterFragment) {
                        val nextPopFragment = supportFragmentManager
                            .getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 2)
                        if (nextPopFragment.name == HOME_FRAGMENT_COMMIT) {
                            lastFragmentInBackStack.prepareTransitionBeforeNewFragment(true)
                        } else if (nextPopFragment.name == FAVORITES_FRAGMENT_COMMIT) {
                            lastFragmentInBackStack.prepareTransitionBeforeNewFragment(false)
                        }
                    }
                }
                val backPressedTime = System.currentTimeMillis()
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
                        supportFragmentManager.popBackStackWithSavingFragments()
                    }
                } else {
                    supportFragmentManager.popBackStackWithSavingFragments()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == DetailsFragment.EXTERNAL_WRITE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is DetailsFragment) {
                        fragment.performAsyncLoadOfPoster()
                        return
                    }
                }
            }
        }
    }

    private fun setNavigationBarAppearance() {
        binding.navigationBar.apply {
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
                    outline?.alpha = 0f
                }
            }
            clipToOutline = true
        }
    }


    companion object {
        const val MOVIE_DATA_KEY = "MOVIE"
        const val POSTER_TRANSITION_KEY = "POSTER_TRANSITION"

        private const val HOME_FRAGMENT_COMMIT = "HOME_FRAGMENT_COMMIT"
        private const val FAVORITES_FRAGMENT_COMMIT = "FAVORITES_FRAGMENT_COMMIT"
        private const val WATCH_LATER_FRAGMENT_COMMIT = "WATCH_LATER_FRAGMENT_COMMIT"

        private const val BACK_DOUBLE_TAP_THRESHOLD = 1500L
        private const val ONE_FRAGMENT_IN_STACK = 1
        private const val LOOP_CYCLE_DELAY = 50L
    }
}