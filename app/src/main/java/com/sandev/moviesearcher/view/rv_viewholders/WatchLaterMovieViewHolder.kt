package com.sandev.moviesearcher.view.rv_viewholders

import android.text.format.DateFormat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.domain.constants.TmdbCommonConstants
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.WatchLaterMovieCardBinding
import java.util.Date


class WatchLaterMovieViewHolder(private val binding: WatchLaterMovieCardBinding)
    : RecyclerView.ViewHolder(binding.root), MovieBinding, PosterBinding, RatingDonut {

    override val poster = binding.movieCardPoster
    override val ratingDonut = binding.ratingDonut
    val scheduleButton = binding.scheduleButton

    private val dateFormat = DateFormat.getMediumDateFormat(App.instance)
    private val timeFormat = DateFormat.getTimeFormat(App.instance)

    override fun onBind(databaseMovieData: DatabaseMovie, position: Int) {
        databaseMovieData as WatchLaterDatabaseMovie

        val poster = binding.movieCardPoster
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
            binding.root.resources.getString(R.string.movie_view_holder_transition_name, position)
        binding.movieCardMovieTitle.text = databaseMovieData.title
        binding.movieCardNotificationDate.text =
            if (databaseMovieData.notificationDate != null)
                "${dateFormat.format(Date(databaseMovieData.notificationDate!!))} ${timeFormat.format(Date(databaseMovieData.notificationDate!!))}"
            else
                App.instance.getString(R.string.watch_later_movie_card_body_notification_scheduled_time_null)
        binding.ratingDonut.setProgress((databaseMovieData.rating * MOVIE_RATING_MULTIPLIER).toInt())
    }


    companion object {
        const val MOVIE_RATING_MULTIPLIER = 10
    }
}