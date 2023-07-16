package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplForSavedLists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

    private val deletedMovie = MutableLiveData<Movie>()
    val getDeletedMovie: LiveData<Movie> = deletedMovie

    private val movieAddedNotify = MutableLiveData<Nothing>()
    val getMovieAddedNotify: LiveData<Nothing> = movieAddedNotify


    suspend fun addToList(movie: Movie) = withContext(Dispatchers.IO) {
        repo.putToDB(listOf(movie))
        movieAddedNotify.postValue(null)
    }

    suspend fun removeFromList(movie: Movie) = withContext(Dispatchers.IO) {
        (repo as MoviesListRepositoryImplForSavedLists).deleteFromDB(movie)
        deletedMovie.postValue(movie)
    }

    suspend fun getAllFromList(): LiveData<List<Movie>> = withContext(Dispatchers.IO) {
        repo.getAllFromDB()
    }

    suspend fun getFewMoviesFromList(from: Int, moviesCount: Int): List<Movie>
            = withContext(Dispatchers.IO) {
        repo.getFromDB(from = from, moviesCount = moviesCount)
    }

    suspend fun getFewSearchedMoviesFromList(query: String, from: Int, moviesCount: Int): List<Movie>
            = withContext(Dispatchers.IO) {
        repo.getSearchedFromDB(query = query, from = from, moviesCount = moviesCount)
    }


    companion object {
        const val MOVIES_PER_PAGE = 15
    }
}