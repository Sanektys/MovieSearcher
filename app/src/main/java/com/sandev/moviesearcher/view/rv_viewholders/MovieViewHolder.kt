package com.sandev.moviesearcher.view.rv_viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.MovieCardBinding
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiConstants.IMAGES_URL
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiConstants.IMAGE_MEDIUM_SIZE


class MovieViewHolder(private val binding: MovieCardBinding) : RecyclerView.ViewHolder(binding.root) {
    val poster = binding.movieCardPoster

    companion object {
        const val MOVIE_RATING_MULTIPLIER = 10
    }

    fun onBind(movieData: Movie, position: Int) {
        if (movieData.poster != null) {
            Glide.with(binding.root)
                .load("${IMAGES_URL}${IMAGE_MEDIUM_SIZE}${movieData.poster}")
                .placeholder(R.drawable.dummy_poster)
                .into(poster)
        } else {
            Glide.with(binding.root)
                .load(R.drawable.dummy_poster)
                .into(poster)
        }
        poster.transitionName =
            binding.root.resources.getString(R.string.movie_view_holder_transition_name, position)
        binding.movieCardMovieTitle.text = movieData.title
        binding.movieCardMovieDescription.text = movieData.description
        binding.ratingDonut.setProgress((movieData.rating * MOVIE_RATING_MULTIPLIER).toInt())
    }
}