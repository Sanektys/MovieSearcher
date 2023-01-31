package com.sandev.moviesearcher

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuButtonsInitial()
        addPosters()
    }

    private fun menuButtonsInitial() {
        val settingsButton: View = findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(this, R.animator.settings_button_spin)

        val appToolbar: Toolbar = findViewById(R.id.app_toolbar)
        appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(this, R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        val navigationBar: NavigationBarView = findViewById(R.id.navigation_bar)
        navigationBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_navigation_favorites_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.bottom_navigation_watch_later_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.bottom_navigation_selections_button -> {
                    Toast.makeText(this, it.title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun addPosters() {
        val postersContainer = findViewById<LinearLayoutCompat>(R.id.posters_container)
        postersContainer.removeAllViews()

        for (i in 1..8) {
            val posterCard = layoutInflater.inflate(R.layout.poster_card, postersContainer, false)
            when (i) {
                1 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_1)
                2 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_2)
                3 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_3)
                4 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_4)
                5 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_5)
                6 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_6)
                7 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_7)
                8 -> posterCard.findViewById<ImageView>(R.id.poster_picture).setImageResource(R.drawable.poster_8)
            }
            postersContainer.addView(posterCard)
        }
        postersContainer.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.posters_appearance)
    }
}