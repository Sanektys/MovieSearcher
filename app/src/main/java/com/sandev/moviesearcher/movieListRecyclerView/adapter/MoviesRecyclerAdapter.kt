package com.sandev.moviesearcher.movieListRecyclerView.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.diffUtil.MoviesListDiff
import com.sandev.moviesearcher.movieListRecyclerView.viewHolder.MovieViewHolder


class MoviesRecyclerAdapter : RecyclerView.Adapter<MovieViewHolder>() {
    private val moviesList: MutableList<Movie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MovieViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_card, parent, false))

    override fun getItemCount() = moviesList.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.onBind(moviesList[position])
    }

    fun setList(newList: List<Movie>) {
        val oldList = moviesList.toList()
        moviesList.clear()
        moviesList.addAll(newList)
        DiffUtil.calculateDiff(MoviesListDiff(oldList, newList)).dispatchUpdatesTo(this)
    }

    fun addMovieCard(movie: Movie) {
        moviesList.add(movie)
        notifyItemInserted(moviesList.size - 1)
    }

    fun addMovieCards(newCards: List<Movie>) {
        val oldList = moviesList.toList()
        moviesList.addAll(newCards)
        DiffUtil.calculateDiff(MoviesListDiff(oldList, moviesList)).dispatchUpdatesTo(this)
    }
}