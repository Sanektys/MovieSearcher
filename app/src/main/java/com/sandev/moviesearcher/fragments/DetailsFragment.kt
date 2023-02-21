package com.sandev.moviesearcher.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.transition.TransitionInflater
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class DetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
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
            Snackbar.make(requireContext(), view, "Added to favorites", Snackbar.LENGTH_SHORT).show()
        }
        toWatchLaterButton.setOnClickListener {
            Snackbar.make(requireContext(), view, "Added to watch later", Snackbar.LENGTH_SHORT).show()
        }
        shareButton.setOnClickListener {
            Snackbar.make(requireContext(), view, "Going to share", Snackbar.LENGTH_SHORT).show()
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
}