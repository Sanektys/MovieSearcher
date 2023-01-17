package com.sandev.moviesearcher

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuButtonsInit()
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
}