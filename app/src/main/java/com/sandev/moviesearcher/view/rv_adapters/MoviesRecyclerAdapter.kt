package com.sandev.moviesearcher.view.rv_adapters

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.databinding.MovieCardBinding
import com.sandev.moviesearcher.utils.rv_diffutils.MoviesListDiff
import com.sandev.moviesearcher.view.rv_viewholders.MovieViewHolder
import java.util.Collections
import java.util.WeakHashMap


class MoviesRecyclerAdapter(private var isDonutAnimationEnabled: Boolean)
    : RecyclerView.Adapter<MovieViewHolder>() {

    val sharedPreferencesCallback: SharedPreferences.OnSharedPreferenceChangeListener = initializeSharedPreferencesCallback()

    private val moviesList: MutableList<DatabaseMovie> = mutableListOf()
    private var clickListener: OnClickListener? = null
    var lastClickedMoviePosition = DEFAULT_NON_CLICKED_POSITION
        private set

    private val viewHolders = Collections.newSetFromMap(WeakHashMap<MovieViewHolder, Boolean>())


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val viewHolder = MovieViewHolder(MovieCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        viewHolder.ratingDonut.isRatingAnimationEnabled = isDonutAnimationEnabled
        viewHolders.add(viewHolder)

        return viewHolder
    }

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
        fun onClick(databaseMovie: DatabaseMovie, posterView: ShapeableImageView)
    }

    fun setPosterOnClickListener(onClickListener: OnClickListener?) {
        clickListener = onClickListener
    }

    fun getMovieAt(position: Int): DatabaseMovie? = moviesList.getOrNull(position)

    fun findMovie(databaseMovie: DatabaseMovie): DatabaseMovie? = moviesList.find { it.title == databaseMovie.title }

    fun setList(newList: List<DatabaseMovie>?) {
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

    fun addMovieCard(databaseMovie: DatabaseMovie) {
        moviesList.add(databaseMovie)
        notifyItemInserted(moviesList.size - 1)
    }

    fun addMovieCards(newCards: List<DatabaseMovie>?) {
        if (newCards?.isNotEmpty() == true) {
            val oldSize = moviesList.size
            moviesList.addAll(newCards)
            notifyItemRangeInserted(oldSize, newCards.size)
        }
    }

    fun removeMovieCard(databaseMovie: DatabaseMovie): Boolean {
        val index = moviesList.indexOf(databaseMovie)
        if (index == -1) return false
        moviesList.removeAt(index)
        notifyItemRemoved(index)
        return true
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

    private fun initializeSharedPreferencesCallback()
            = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == SharedPreferencesProvider.KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON
            || key == SharedPreferencesProvider.KEY_ENABLE_RATING_DONUT_ANIMATION) {

            val isRatingDonutSwitchButtonEnabled = sharedPreferences.getBoolean(SharedPreferencesProvider.KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON, false)
            val isRatingDonutAnimationEnabled = sharedPreferences.getBoolean(SharedPreferencesProvider.KEY_ENABLE_RATING_DONUT_ANIMATION, false)
            isDonutAnimationEnabled = isRatingDonutAnimationEnabled && isRatingDonutSwitchButtonEnabled

            updateAnimationStateInItems()
        }
    }

    private fun updateAnimationStateInItems() {
        for (item in viewHolders) {
            item.ratingDonut.isRatingAnimationEnabled = isDonutAnimationEnabled
        }
    }


    companion object {
        const val DEFAULT_NON_CLICKED_POSITION = -1
    }
}