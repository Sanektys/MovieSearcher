package com.sandev.moviesearcher.movieListRecyclerView.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.views.RatingDonutView


class MovieViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    val poster: ImageView = view.findViewById(R.id.movie_card_poster)
    private val title: TextView = view.findViewById(R.id.movie_card_movie_title)
    private val description: TextView = view.findViewById(R.id.movie_card_movie_description)
    private val ratingDonut: RatingDonutView = view.findViewById(R.id.rating_donut)

    fun onBind(movieData: Movie, position: Int) {
        Glide.with(view).load(movieData.poster).centerCrop().into(poster)
        poster.transitionName = poster.resources.getString(R.string.movie_view_holder_transition_name,
            position)
        title.text = movieData.title
        description.text = movieData.description
        ratingDonut.setProgress((movieData.rating * 10).toInt())
    }
}