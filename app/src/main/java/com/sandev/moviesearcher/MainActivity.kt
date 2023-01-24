package com.sandev.moviesearcher

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        var button: Button = findViewById(R.id.center_container_button1)
        button.setOnClickListener {
            Toast.makeText(this,  R.string.activity_main_center_container_button1_text, Toast.LENGTH_SHORT).show()
        }
        button = findViewById(R.id.center_container_button2)
        button.setOnClickListener {
            Toast.makeText(this, R.string.activity_main_center_container_button2_text, Toast.LENGTH_SHORT).show()
        }
        button = findViewById(R.id.center_container_button3)
        button.setOnClickListener {
            Toast.makeText(this, R.string.activity_main_center_container_button3_text, Toast.LENGTH_SHORT).show()
        }
        button = findViewById(R.id.center_container_button4)
        button.setOnClickListener {
            Toast.makeText(this, R.string.activity_main_center_container_button4_text, Toast.LENGTH_SHORT).show()
        }
        button = findViewById(R.id.center_container_button5)
        button.setOnClickListener {
            Toast.makeText(this, R.string.activity_main_center_container_button5_text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun addPosters() {
        val postersContainer = findViewById<LinearLayout>(R.id.posters_container)
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