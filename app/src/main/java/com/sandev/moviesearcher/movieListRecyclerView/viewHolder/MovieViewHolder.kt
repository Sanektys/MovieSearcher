package com.sandev.moviesearcher.movieListRecyclerView.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val poster: ImageView = view.findViewById(R.id.movie_card_poster_picture)
    private val title: TextView = view.findViewById(R.id.movie_card_movie_title)
    private val description: TextView = view.findViewById(R.id.movie_card_movie_description)

    fun onBind(movieData: Movie, position: Int) {
        poster.setImageResource(movieData.poster)
        poster.transitionName = "PosterTransition$position"
        title.text = movieData.title
        description.text = movieData.description
    }
}