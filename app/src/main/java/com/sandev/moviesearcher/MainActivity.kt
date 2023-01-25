package com.sandev.moviesearcher

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.view.marginEnd

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuButtonsInit()
        addPosters()
    }

    private fun menuButtonsInit() {

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
    }
}