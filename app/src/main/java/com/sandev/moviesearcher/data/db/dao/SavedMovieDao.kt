package com.sandev.moviesearcher.data.db.dao

import com.sandev.moviesearcher.data.db.entities.TitleAndDescription


interface SavedMovieDao : MovieDao {

    fun deleteFromCachedMovies(certainMovie: TitleAndDescription): Int
}