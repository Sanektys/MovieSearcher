package com.sandev.moviesearcher.movieListRecyclerView.data

import android.os.Parcelable
import com.sandev.moviesearcher.R
import kotlinx.parcelize.Parcelize


@Parcelize
data class Movie(val poster: Int, val title: String?,
                 val description: String?, var isFavorite: Boolean = false) : Parcelable

val favoriteMovies = mutableListOf<Movie>()

fun setMockData() = listOf<Movie>(
    Movie(R.drawable.poster_1,"Pearl", "In 1918, a young woman on the brink of madness pursues stardom in a desperate attempt to escape the drudgery, isolation and lovelessness of life on her parents' farm."),
    Movie(R.drawable.poster_2, "The Seven Year Itch", "When his family goes away for the summer, a hitherto faithful husband with an overactive imagination is tempted by a beautiful neighbor."),
    Movie(R.drawable.poster_3, "Iron Man", "After being held captive in an Afghan cave, billionaire engineer Tony Stark creates a unique weaponized suit of armor to fight evil."),
    Movie(R.drawable.poster_4, "The Godfather", "The aging patriarch of an organized crime dynasty in postwar New York City transfers control of his clandestine empire to his reluctant youngest son."),
    Movie(R.drawable.poster_5, "Andor", "Prequel series to Star Wars' 'Rogue One'. In an era filled with danger, deception and intrigue, Cassian will embark on the path that is destined to turn him into a Rebel hero."),
    Movie(R.drawable.poster_6, "Home Alone", "An eight-year-old troublemaker, mistakenly left home alone, must defend his home against a pair of burglars on Christmas eve."),
    Movie(R.drawable.poster_7, "Stranger Things", "When a young boy disappears, his mother, a police chief and his friends must confront terrifying supernatural forces in order to get him back."),
    Movie(R.drawable.poster_8, "A Man Called Otto", "Otto is a grump who's given up on life following the loss of his wife and wants to end it all. When a young family moves in nearby, he meets his match in quick-witted Marisol, leading to a friendship that will turn his world around.")
)
