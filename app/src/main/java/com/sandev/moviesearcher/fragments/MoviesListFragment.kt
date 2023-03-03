package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R


abstract class MoviesListFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance(view)
        initializeToolbar(view)
    }

    private fun initializeToolbar(view: View) {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        val appToolbar: MaterialToolbar = view.findViewById(R.id.app_toolbar)
        appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(requireContext(), R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setAppBarAppearance(rootView: View) {
        rootView.findViewById<AppBarLayout>(R.id.app_bar).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                insets
            }
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, -MainActivity.APP_BARS_CORNER_RADIUS.toInt(),
                        view!!.width, view.height,
                        MainActivity.APP_BARS_CORNER_RADIUS
                    )
                }
            }
            clipToOutline = true
        }
    }
}