package com.sandev.moviesearcher.view.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentSplashScreenBinding
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import com.sandev.tmdb_feature.TmdbComponentViewModel


class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    private var binding: FragmentSplashScreenBinding? = null

    private val preloadViewModel: HomeFragmentViewModel by lazy {
        val tmdbComponent = ViewModelProvider(requireActivity())[TmdbComponentViewModel::class.java]

        val viewModelFactory = HomeFragmentViewModel.ViewModelFactory(tmdbComponent.interactor)
        ViewModelProvider(requireActivity(), viewModelFactory)[HomeFragmentViewModel::class.java]
    }

    companion object {
        private const val REMOVING_DELAY = 100L

        var isSplashWasCreated = false
            private set
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSplashScreenBinding.bind(view)

        preloadViewModel
        if (!isSplashWasCreated) {
            initializeSplashScreen()
        } else {
            initializeRemovingSplashScreen()
        }
    }

    override fun onStop() {
        super.onStop()

        binding?.splashAnimation?.cancelAnimation()
    }

    private fun initializeSplashScreen() {
        requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar).doOnPreDraw { bottomNavigation ->
            val navigationHeight = bottomNavigation.height.toFloat() / 2f

            binding?.splashAnimation?.let { lottie ->
                lottie.translationY = navigationHeight

                lottie.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}

                    override fun onAnimationCancel(animation: Animator) {
                        binding?.splashAnimation?.removeAllAnimatorListeners()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        isSplashWasCreated = true
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
        }
    }

    private fun initializeRemovingSplashScreen() {
        binding?.splashAnimation?.apply {
            cancelAnimation()
            alpha = 0f
            postDelayed(REMOVING_DELAY) {  // Если запускать удаление сразу, то ломается transition animation
                (activity as MainActivity).removeSplashScreen(this@SplashScreenFragment)
            }
        }
    }
}
