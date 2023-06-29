package com.sandev.moviesearcher.utils.rv_animators

import android.content.res.Configuration
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView


class MovieItemAnimator : DefaultItemAnimator() {

    companion object {
        const val REMOVE_DURATION = 300L
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.run {
            if (itemView.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                itemView.animate()
                    .translationX(itemView.width.toFloat() + 100f)
                    .setDuration(REMOVE_DURATION)
                    .setInterpolator(FastOutLinearInInterpolator())
                    .withStartAction { dispatchRemoveStarting(holder) }
                    .withEndAction {
                        dispatchRemoveFinished(holder)
                        itemView.translationX = 0f
                    }
                    .start()
            } else if (itemView.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemView.animate()
                    .translationY(-itemView.height.toFloat() - 50f)
                    .setDuration(REMOVE_DURATION)
                    .setInterpolator(FastOutLinearInInterpolator())
                    .withStartAction { dispatchRemoveStarting(holder) }
                    .withEndAction {
                        dispatchRemoveFinished(holder)
                        itemView.translationY = 0f
                    }
                    .start()
            }
        }
        return true
    }

    override fun getRemoveDuration() = REMOVE_DURATION
}