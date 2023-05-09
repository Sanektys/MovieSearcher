package com.sandev.moviesearcher.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Movie(val poster: String, val title: String, val description: String, var rating: Float = 0f) : Parcelable
