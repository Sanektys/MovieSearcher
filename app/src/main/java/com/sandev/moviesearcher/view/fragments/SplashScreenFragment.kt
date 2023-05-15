package com.sandev.moviesearcher.view.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashScreenFragment : Fragment() {

    private val preloadViewModel: HomeFragmentViewModel by lazy {
        ViewModelProvider(requireActivity())[HomeFragmentViewModel::class.java]
    }

    companion object {
        private const val REMOVING_DELAY = 100L

        var isSplashWasCreated = false
            private set
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        preloadViewModel
        if (!isSplashWasCreated) {
            initializeSplashScreen()
        } else {
            initializeRemovingSplashScreen()
        }
    }

    private fun initializeSplashScreen() {
        requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar).doOnPreDraw { bottomNavigation ->
            val navigationHeight = bottomNavigation.height.toFloat() / 2f

            requireView().findViewById<LottieAnimationView>(R.id.splash_animation).let { lottie ->
                lottie.translationY = navigationHeight

                lottie.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        (activity as MainActivity).startHomeFragment()

                        lottie.animate()
                            .scaleX(0f)
                            .scaleY(0f)
                            .setInterpolator(AccelerateInterpolator())
                            .setDuration(resources.getInteger(R.integer.activity_main_animations_durations_splash_screen_disappearing).toLong())
                            .withEndAction {
                                (activity as? MainActivity)?.removeSplashScreen(this@SplashScreenFragment)
                            }
                            .start()
                    }
                })
            }
            isSplashWasCreated = true
        }
    }

    private fun initializeRemovingSplashScreen() {
        requireView().findViewById<LottieAnimationView>(R.id.splash_animation).apply {
            cancelAnimation()
            alpha = 0f
            postDelayed(REMOVING_DELAY) {  // Если запускать удаление сразу, то ломается transition animation
                (activity as MainActivity).removeSplashScreen(this@SplashScreenFragment)
            }
        }
    }
}
