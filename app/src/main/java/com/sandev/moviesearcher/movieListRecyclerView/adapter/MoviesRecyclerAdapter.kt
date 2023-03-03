package com.sandev.moviesearcher.movieListRecyclerView.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.diffUtil.MoviesListDiff
import com.sandev.moviesearcher.movieListRecyclerView.viewHolder.MovieViewHolder


class MoviesRecyclerAdapter : RecyclerView.Adapter<MovieViewHolder>() {

    private val moviesList: MutableList<Movie> = mutableListOf()
    private lateinit var clickListener: OnClickListener
    var lastMovieClickedPosition = -1
        private set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MovieViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.movie_card, parent, false))

    override fun getItemCount() = moviesList.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.onBind(moviesList[position], position)
        holder.itemView.findViewById<View>(R.id.movie_card_poster).setOnClickListener {
            lastMovieClickedPosition = holder.bindingAdapterPosition
            it.transitionName = it.resources.getString(R.string.movie_view_holder_transition_name,
                lastMovieClickedPosition)
            clickListener.onClick(moviesList[lastMovieClickedPosition], holder.poster)
        }
    }

    interface OnClickListener {
        fun onClick(movie: Movie, posterView: ImageView)
    }

    fun setPosterOnClickListener(onClickListener: OnClickListener) {
        clickListener = onClickListener
    }

    fun getMovieAt(position: Int): Movie? = moviesList.getOrNull(position)

    fun findMovie(movie: Movie): Movie? = moviesList.find { it.title == movie.title }

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

    fun removeMovieCard(movie: Movie) {
        val index = moviesList.indexOf(movie)
        moviesList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeMovieCardAt(position: Int) {
        moviesList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeLastClickedMovie() {
        if (lastMovieClickedPosition >= 0) {
            moviesList.removeAt(lastMovieClickedPosition)
            notifyItemRemoved(lastMovieClickedPosition)
            lastMovieClickedPosition = -1
        }
    }
}