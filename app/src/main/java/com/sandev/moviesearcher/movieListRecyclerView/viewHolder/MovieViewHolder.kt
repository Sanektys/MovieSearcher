package com.sandev.moviesearcher.movieListRecyclerView.viewHolder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie


class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val poster: ImageView = view.findViewById(R.id.movie_card_poster)
    private val title: TextView = view.findViewById(R.id.movie_card_movie_title)
    private val description: TextView = view.findViewById(R.id.movie_card_movie_description)

    fun onBind(movieData: Movie, position: Int) {
        poster.setImageResource(movieData.poster)
        poster.transitionName = poster.resources.getString(R.string.movie_view_holder_transition_name,
            position)
        title.text = movieData.title
        description.text = movieData.description
    }
}