package com.sandev.moviesearcher

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData

class MainActivity : AppCompatActivity() {
    private lateinit var moviesRecyclerAdapter: MoviesRecyclerAdapter
    private lateinit var moviesRecyclerManager: RecyclerView.LayoutManager
    private val MOVIES_RECYCLER_VIEW_STATE: String = "MoviesRecylerViewState"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuButtonsInitial()
        addMoviesCards()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager.onSaveInstanceState())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        moviesRecyclerManager.onRestoreInstanceState(savedInstanceState.getBundle(MOVIES_RECYCLER_VIEW_STATE))
    }

    private fun menuButtonsInitial() {
        val settingsButton: View = findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.settings_button_spin)

        val appToolbar: Toolbar = findViewById(R.id.app_toolbar)
        appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(this, R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
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

    private fun addMoviesCards() {
        moviesRecyclerAdapter = MoviesRecyclerAdapter()
        moviesRecyclerAdapter.setList(setMockData())

        val moviesListRecycler: RecyclerView = findViewById(R.id.movies_list_recycler)
        moviesListRecycler.adapter = moviesRecyclerAdapter
        moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.posters_appearance)

        moviesRecyclerManager = moviesListRecycler.layoutManager!!
    }
}