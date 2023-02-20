package com.sandev.moviesearcher

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationBarView
import com.sandev.moviesearcher.fragments.DetailsFragment
import com.sandev.moviesearcher.fragments.HomeFragment
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData

class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuButtonsInitial()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment, HomeFragment())
            .addToBackStack(null)
            .commit()
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager.onSaveInstanceState())
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        moviesRecyclerManager.onRestoreInstanceState(savedInstanceState.getBundle(MOVIES_RECYCLER_VIEW_STATE))
//    }

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

    fun startDetailsFragment(movie: Movie) {
        val bundle = Bundle()
        bundle.putParcelable("Movie", movie)

        val detailsFragment = DetailsFragment().apply {
            arguments = bundle
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, detailsFragment)
            .addToBackStack(null)
            .commit()
    }
}