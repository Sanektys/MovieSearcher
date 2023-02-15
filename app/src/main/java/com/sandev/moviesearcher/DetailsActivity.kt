package com.sandev.moviesearcher

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        initializeContent()
        setFloatButtonOnClick()
    }

    private fun initializeContent() {
        val movie = intent.extras?.get("Movie") as Movie

        val moviePoster: AppCompatImageView = findViewById(R.id.collapsing_toolbar_image)
        val titleText: TextView = findViewById(R.id.title)
        val descriptionText: TextView = findViewById(R.id.description)

        moviePoster.setImageResource(movie.poster)
        titleText.text = movie.title
        descriptionText.text = movie.description
    }

    private fun setFloatButtonOnClick() {
        val toFavoriteButton: FloatingActionButton = findViewById(R.id.fab_to_favorite)
        val toWatchLaterButton: FloatingActionButton = findViewById(R.id.fab_to_watch_later)
        val shareButton: FloatingActionButton = findViewById(R.id.fab_share)
        val detailsLayout: View = findViewById(R.id.details)

        toFavoriteButton.setOnClickListener {
            Snackbar.make(this, detailsLayout, "Added to favorites", Snackbar.LENGTH_SHORT).show()
        }
        toWatchLaterButton.setOnClickListener {
            Snackbar.make(this, detailsLayout, "Added to watch later", Snackbar.LENGTH_SHORT).show()
        }
        shareButton.setOnClickListener {
            Snackbar.make(this, detailsLayout, "Going to share", Snackbar.LENGTH_SHORT).show()
        }
    }
}