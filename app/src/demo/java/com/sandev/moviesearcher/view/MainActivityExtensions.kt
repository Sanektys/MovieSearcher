package com.sandev.moviesearcher.view

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.get
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.ActivityMainBinding
import java.util.Calendar
import java.util.Date


fun MainActivity.showDemoInfoScreen(view: View, isAnimated: Boolean, okButtonCallback: (() -> Unit)? = null) {
    val binding = ActivityMainBinding.bind(view)

    binding.demoInfo.infoButton.setOnClickListener {
        okButtonCallback?.invoke()
        performExitAnimation(binding)
    }

    setupAppearance(binding, isAnimated)
    if (isAnimated) {
        performEnterAnimation(binding)
    } else {
        setupMenuItemsPointersAppearance(binding)
    }
}

fun MainActivity.checkDemoExpired(): Boolean {
    val appInstallationTime = packageManager.getPackageInfo(packageName, 0).firstInstallTime
    val demoFullAccessExpirationDate = Calendar.getInstance().apply {
        time = Date(appInstallationTime)
        add(Calendar.DAY_OF_YEAR, resources.getInteger(R.integer.demo_general_unblocked_features_lifespan))
    }
    val currentDate = Calendar.getInstance().apply { time = Date(System.currentTimeMillis()) }

    return currentDate.after(demoFullAccessExpirationDate)
}

fun MainActivity.checkDemoExpiredWithToast(@StringRes featureExpiredMessage: Int): Boolean {
    return if (checkDemoExpired()) {
        Toast.makeText(this, getString(featureExpiredMessage), Toast.LENGTH_SHORT).show()
        true
    } else false
}

private fun performEnterAnimation(binding: ActivityMainBinding) {
    val resources = binding.root.resources
    val animationDuration = resources.getInteger(R.integer.home_fragment_info_screen_enter_animation_duration).toLong()
    val scale = ResourcesCompat.getFloat(resources, R.dimen.home_fragment_info_screen_animation_enter_scale)

    binding.demoInfo.root.fadeInAnimation(animationDuration)
    binding.demoInfo.infoMessage.fromScaleAnimation(scale, animationDuration)
    binding.demoInfo.infoButton.fromScaleAnimation(scale, animationDuration)

    binding.navigationBar.animate().setListener(object : AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {
            binding.navigationBar.animate().setListener(null)

            setupMenuItemsPointersAppearance(binding)

            binding.demoInfo.favoriteInfoArrow.fadeInFromScaleAnimation(scale, animationDuration)
            binding.demoInfo.watchLaterInfoArrow.fadeInFromScaleAnimation(scale, animationDuration)
            binding.demoInfo.infoWatchLaterLabel.fadeInFromScaleAnimation(scale, animationDuration)
            binding.demoInfo.infoFavoriteLaterLabel.fadeInFromScaleAnimation(scale, animationDuration)
            binding.demoInfo.watchLaterInfoHighlight.fadeInAnimation(animationDuration)
            binding.demoInfo.favoriteInfoHighlight.fadeInAnimation(animationDuration)
        }
    })
}

private fun performExitAnimation(binding: ActivityMainBinding) {
    val resources = binding.root.resources
    val animationDuration = resources.getInteger(R.integer.home_fragment_info_screen_exit_animation_duration).toLong()
    val scale = ResourcesCompat.getFloat(resources, R.dimen.home_fragment_info_screen_animation_exit_scale)

    binding.demoInfo.root.fadeOutAnimation(animationDuration).withEndAction { binding.demoInfo.root.visibility = View.GONE }
    binding.demoInfo.infoMessage.toScaleAnimation(scale, animationDuration)
    binding.demoInfo.infoButton.toScaleAnimation(scale, animationDuration)
    binding.demoInfo.favoriteInfoArrow.toScaleAnimation(scale, animationDuration)
    binding.demoInfo.watchLaterInfoArrow.toScaleAnimation(scale, animationDuration)
    binding.demoInfo.infoWatchLaterLabel.toScaleAnimation(scale, animationDuration)
    binding.demoInfo.infoFavoriteLaterLabel.toScaleAnimation(scale, animationDuration)
}

private fun View.fromScaleAnimation(scale: Float, duration: Long): ViewPropertyAnimator {
    scaleY = scale
    scaleX = scale

    return animate()
        .setDuration(duration)
        .scaleX(1f)
        .scaleY(1f)
}

private fun View.toScaleAnimation(scale: Float, duration: Long): ViewPropertyAnimator {
    return animate()
        .setDuration(duration)
        .scaleX(scale)
        .scaleY(scale)
}

private fun View.fadeInFromScaleAnimation(scale: Float, duration: Long): ViewPropertyAnimator {
    scaleY = scale
    scaleX = scale

    return animate()
        .setDuration(duration)
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
}

private fun View.fadeInAnimation(duration: Long): ViewPropertyAnimator {
    alpha = 0f

    return animate()
        .setDuration(duration)
        .alpha(1f)
}

private fun View.fadeOutAnimation(duration: Long): ViewPropertyAnimator {
    return animate()
        .setDuration(duration)
        .alpha(0f)
}

private fun setupAppearance(binding: ActivityMainBinding, isAnimated: Boolean) {
    binding.demoInfo.root.visibility = View.VISIBLE

    val resources = binding.root.resources
    val message = resources.getString(R.string.home_fragment_info_screen_message, resources.getInteger(R.integer.demo_general_unblocked_features_lifespan))
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        (binding.demoInfo.infoMessage.getChildAt(0) as TextView).text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
    } else {
        (binding.demoInfo.infoMessage.getChildAt(0) as TextView).text = Html.fromHtml(message)
    }

    if (isAnimated) {
        binding.demoInfo.infoWatchLaterLabel.alpha = 0f
        binding.demoInfo.watchLaterInfoArrow.alpha = 0f
        binding.demoInfo.watchLaterInfoHighlight.alpha = 0f

        binding.demoInfo.infoFavoriteLaterLabel.alpha = 0f
        binding.demoInfo.favoriteInfoArrow.alpha = 0f
        binding.demoInfo.favoriteInfoHighlight.alpha = 0f
    }
}

private fun setupMenuItemsPointersAppearance(binding: ActivityMainBinding) {
    val watchLaterMenuButton = (binding.navigationBar[0] as ViewGroup)[1]
    val favoriteMenuButton = (binding.navigationBar[0] as ViewGroup)[2]

    binding.navigationBar.doOnPreDraw {
        val resources = binding.root.resources
        val navigationViewOriginalHeight = resources.getDimensionPixelSize(R.dimen.activity_main_bottom_navigation_bar_height)
        val navigationViewDeltaHeight = binding.navigationBar.height - navigationViewOriginalHeight

        val buttonCoordinate = IntArray(2)
        favoriteMenuButton.getLocationInWindow(buttonCoordinate)
        var menuButtonCenterX = buttonCoordinate[0] + (favoriteMenuButton.width / 2)
        var menuButtonCenterY = buttonCoordinate[1] + (favoriteMenuButton.height / 2)

        binding.demoInfo.infoFavoriteLaterLabel.apply {
            x = menuButtonCenterX - width / 2f
            y -= navigationViewDeltaHeight
        }
        binding.demoInfo.favoriteInfoArrow.apply {
            x = menuButtonCenterX - width / 2f
            y -= navigationViewDeltaHeight
        }
        binding.demoInfo.favoriteInfoHighlight.apply {
            x = menuButtonCenterX - width / 2f
            y = menuButtonCenterY - height / 2f
        }

        watchLaterMenuButton.getLocationInWindow(buttonCoordinate)
        menuButtonCenterX = buttonCoordinate[0] + (watchLaterMenuButton.width / 2)
        menuButtonCenterY = buttonCoordinate[1] + (watchLaterMenuButton.height / 2)

        binding.demoInfo.infoWatchLaterLabel.apply {
            x = menuButtonCenterX - width / 2f
            y -= navigationViewDeltaHeight
        }
        binding.demoInfo.watchLaterInfoArrow.apply {
            x = menuButtonCenterX - width / 2f
            y -= navigationViewDeltaHeight
        }
        binding.demoInfo.watchLaterInfoHighlight.apply {
            x = menuButtonCenterX - width / 2f
            y = menuButtonCenterY - height / 2f
        }
    }
}