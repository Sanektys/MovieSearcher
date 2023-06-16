package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.dao.SavedMovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.db.entities.TitleAndDescription
import java.util.concurrent.Executors


class MoviesListRepositoryImplWithList(moviesDao: MovieDao) : MoviesListRepositoryImpl(moviesDao) {

    private val moviesList = mutableListOf<Movie>()

    val moviesListLiveData = MutableLiveData<List<Movie>>()
    val moviesCountInDbLiveData = MutableLiveData<Int>(0)

    init {
        Executors.newSingleThreadExecutor().execute {
            moviesList.addAll(super.getAllFromDB())
            moviesListLiveData.postValue(moviesList)
            moviesCountInDbLiveData.postValue(moviesList.size)
        }

        putToDbFlagLiveData.observeForever { flags ->
            var successCount = 0
            flags.forEach { flag ->
                if (flag != PUT_ERROR_FLAG) {
                    ++successCount
                }
            }
            moviesCountInDbLiveData.postValue(moviesCountInDbLiveData.value?.let { it + successCount })
        }
        deletedRowsCountLiveData.observeForever { count ->
            moviesCountInDbLiveData.postValue(moviesCountInDbLiveData.value?.let { it - count })
        }
    }


    override fun putToDB(movies: List<Movie>) {
        super.putToDB(movies)
        moviesList.addAll(movies)
    }

    override fun getAllFromDB(): List<Movie> = moviesList.toList()

    fun deleteFromDB(movie: Movie) {
        Executors.newSingleThreadExecutor().execute {
            deletedRowsCountLiveData.postValue(
                (movieDao as SavedMovieDao).deleteFromCachedMovies(
                    TitleAndDescription(title = movie.title, description = movie.description)
                )
            )
        }
        moviesList.remove(movie)
    }

    fun getMoviesCountInList() = moviesList.size

    fun getMoviesCountInDB() = moviesCountInDbLiveData.value!!
}