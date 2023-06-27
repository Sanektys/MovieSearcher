package com.sandev.moviesearcher.view.rv_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.databinding.MovieCardBinding
import com.sandev.moviesearcher.utils.rv_diffutils.MoviesListDiff
import com.sandev.moviesearcher.view.rv_viewholders.MovieViewHolder


class MoviesRecyclerAdapter : RecyclerView.Adapter<MovieViewHolder>() {

    private val moviesList: MutableList<Movie> = mutableListOf()
    private var clickListener: OnClickListener? = null
    var lastClickedMoviePosition = DEFAULT_NON_CLICKED_POSITION
        private set

    companion object {
        const val DEFAULT_NON_CLICKED_POSITION = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MovieViewHolder(MovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = moviesList.size

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.onBind(moviesList[position], position)

        holder.poster.setOnClickListener {
            lastClickedMoviePosition = holder.bindingAdapterPosition
            holder.poster.transitionName = it.resources.getString(R.string.movie_view_holder_transition_name,
                lastClickedMoviePosition)
            clickListener?.onClick(moviesList[lastClickedMoviePosition], holder.poster)
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

    fun setList(newList: List<Movie>?) {
        if (newList == null || newList == moviesList) {
            return
        }
        val oldList = moviesList.toList()
        moviesList.clear()
        moviesList.addAll(newList)
        DiffUtil.calculateDiff(MoviesListDiff(oldList, newList)).dispatchUpdatesTo(this)
    }

    fun clearList() {
        val listSize = moviesList.size
        moviesList.clear()
        notifyItemRangeRemoved(0, listSize)
    }

    fun addMovieCard(movie: Movie) {
        moviesList.add(movie)
        notifyItemInserted(moviesList.size - 1)
    }

    fun addMovieCards(newCards: List<Movie>?) {
        if (newCards?.isNotEmpty() == true) {
            val oldSize = moviesList.size
            moviesList.addAll(newCards)
            notifyItemRangeInserted(oldSize, newCards.size)
        }
    }

    fun removeMovieCard(movie: Movie) {
        val index = moviesList.indexOf(movie)
        if (index == -1) return
        moviesList.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeMovieCardAt(position: Int) {
        moviesList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun removeLastClickedMovie() {
        if (lastClickedMoviePosition >= 0) {
            moviesList.removeAt(lastClickedMoviePosition)
            notifyItemRemoved(lastClickedMoviePosition)
            lastClickedMoviePosition = DEFAULT_NON_CLICKED_POSITION
        }
    }
}