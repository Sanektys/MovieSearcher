package com.sandev.moviesearcher.view.rv_viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.MovieCardBinding
import com.sandev.moviesearcher.domain.Movie


class MovieViewHolder(private val binding: MovieCardBinding) : RecyclerView.ViewHolder(binding.root) {
    val poster = binding.movieCardPoster

    companion object {
        const val MOVIE_RATING_MULTIPLIER = 10
    }

    fun onBind(movieData: Movie, position: Int) {
        Glide.with(binding.root).load(movieData.poster).centerCrop().into(poster)
        poster.transitionName =
            binding.root.resources.getString(R.string.movie_view_holder_transition_name, position)
        binding.movieCardMovieTitle.text = movieData.title
        binding.movieCardMovieDescription.text = movieData.description
        binding.ratingDonut.setProgress((movieData.rating * MOVIE_RATING_MULTIPLIER).toInt())
    }
}