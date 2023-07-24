package com.sandev.moviesearcher.utils

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import kotlin.math.hypot


class CircularRevealAnimator(private val windowHeight: Int, private val windowWidth: Int,
                             private val targetX: Int, private val targetY: Int,
                             private val revealingView: View) {

    private var isReveal: Boolean = revealingView.visibility == View.VISIBLE
    private var isAnimationActive: Boolean = false

    private val inInterpolator = DecelerateInterpolator()
    private val outInterpolator = AccelerateInterpolator()

    var animationDuration = DEFAULT_ANIMATION_DURATION


    fun revealView() {
        if (!isAnimationActive) {
            if (!isReveal) {
                reveal()
            } else {
                hide()
            }
        }
    }

    private fun reveal() {
        val startRadius = 0f
        val endRadius = hypot(windowHeight.toFloat(), windowWidth.toFloat())

        ViewAnimationUtils.createCircularReveal(
                revealingView, targetX, targetY, startRadius, endRadius).apply {
            duration = animationDuration
            interpolator = inInterpolator
            revealingView.visibility = View.VISIBLE
            addListener(animationListener)
            start()
        }
    }

    private fun hide() {
        val startRadius = hypot(windowHeight.toFloat(), windowWidth.toFloat())
        val endRadius = 0f

        ViewAnimationUtils.createCircularReveal(
                revealingView, targetX, targetY, startRadius, endRadius).apply {
            duration = animationDuration
            interpolator = outInterpolator
            addListener(animationListener)
            start()
        }
    }


    private val animationListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
            isAnimationActive = true
        }

        override fun onAnimationEnd(animation: Animator) {
            isAnimationActive = false
            isReveal = !isReveal
            if (!isReveal) {
                revealingView.visibility = View.GONE
            }
        }

        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    }


    companion object {
        const val DEFAULT_ANIMATION_DURATION = 400L
    }
}