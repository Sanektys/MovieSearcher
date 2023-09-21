package com.sandev.moviesearcher.view.rv_viewholders

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.domain.constants.TmdbCommonConstants
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.MovieCardBinding


class MovieViewHolder(private val binding: MovieCardBinding)
    : RecyclerView.ViewHolder(binding.root), MovieBinding, PosterBinding, RatingDonut {

    override val poster = binding.movieCardPoster
    override val ratingDonut = binding.ratingDonut


    override fun onBind(databaseMovieData: DatabaseMovie) {
        if (databaseMovieData.poster != null) {
            Glide.with(binding.root)
                .load("${TmdbCommonConstants.IMAGES_URL}${TmdbCommonConstants.IMAGE_MEDIUM_SIZE}${databaseMovieData.poster}")
                .placeholder(R.drawable.dummy_poster)
                .apply(RequestOptions().dontTransform())
                .into(poster)
        } else {
            Glide.with(binding.root)
                .load(R.drawable.dummy_poster)
                .into(poster)
        }
        poster.transitionName =
            binding.root.resources.getString(R.string.movie_view_holder_transition_name, bindingAdapterPosition)
        binding.movieCardMovieTitle.text = databaseMovieData.title
        binding.movieCardMovieDescription.text = databaseMovieData.description
        binding.ratingDonut.setProgress((databaseMovieData.rating * MOVIE_RATING_MULTIPLIER).toInt())
    }


    companion object {
        const val MOVIE_RATING_MULTIPLIER = 10
    }
}