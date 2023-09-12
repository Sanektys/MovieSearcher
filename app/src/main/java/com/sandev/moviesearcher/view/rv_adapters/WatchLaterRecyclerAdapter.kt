package com.sandev.moviesearcher.view.rv_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sandev.moviesearcher.databinding.WatchLaterMovieCardBinding
import com.sandev.moviesearcher.view.rv_viewholders.WatchLaterMovieViewHolder


class WatchLaterRecyclerAdapter(isDonutAnimationEnabled: Boolean) :
    MoviesRecyclerAdapter(isDonutAnimationEnabled) {

    private var onScheduleNotificationButtonClick: ScheduleNotificationButtonClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder = WatchLaterMovieViewHolder(
            WatchLaterMovieCardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

        viewHolder.ratingDonut.isRatingAnimationEnabled = isDonutAnimationEnabled
        viewHolders.add(viewHolder)

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        (holder as WatchLaterMovieViewHolder).scheduleButton.setOnClickListener {
            onScheduleNotificationButtonClick?.onButtonClick()
        }
    }

    fun setOnScheduleNotificationButtonClick(onClick: ScheduleNotificationButtonClick?) {
        onScheduleNotificationButtonClick = onClick
    }


    interface ScheduleNotificationButtonClick {
        fun onButtonClick()
    }
}