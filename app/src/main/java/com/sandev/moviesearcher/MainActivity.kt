package com.sandev.moviesearcher

import android.content.res.Resources
import android.graphics.Outline
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.sandev.moviesearcher.fragments.DetailsFragment
import com.sandev.moviesearcher.fragments.HomeFragment
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class MainActivity : AppCompatActivity() {

    companion object {
        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"
        const val MOVIE_DATA_KEY = "MOVIE"
        const val POSTER_TRANSITION_KEY = "POSTER_TRANSITION"

        val APP_BARS_CORNER_RADIUS = 28f * Resources.getSystem().displayMetrics.density

        lateinit var moviesRecyclerManager: RecyclerView.LayoutManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Отменяем коллизию status & navigation bars, чтобы наши вьюхи проходили под ними
        WindowCompat.setDecorFitsSystemWindows(window, false)

        menuButtonsInitial()
        setNavigationBarAppearance()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        moviesRecyclerManager.onRestoreInstanceState(savedInstanceState.getParcelable(MOVIES_RECYCLER_VIEW_STATE))
    }

    private fun menuButtonsInitial() {
        val navigationBar: NavigationBarView = findViewById(R.id.navigation_bar)
        navigationBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_navigation_favorites_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.bottom_navigation_watch_later_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.bottom_navigation_selections_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
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
                        view!!.width, (view.height + APP_BARS_CORNER_RADIUS).toInt(),
                        APP_BARS_CORNER_RADIUS
                    )
                }
            }
            clipToOutline = true
        }
    }
}