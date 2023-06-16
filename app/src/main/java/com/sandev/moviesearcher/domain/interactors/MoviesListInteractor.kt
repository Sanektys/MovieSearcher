package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import java.util.*
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

    val moviesListLiveData = MutableLiveData<List<Movie>>()
    val isListAndDbSameSizeLiveData = MutableLiveData<Boolean>()

    init {
        (repo as MoviesListRepositoryImplWithList).moviesListLiveData.observeForever { moviesList ->
            moviesListLiveData.postValue(moviesList)
        }

        (repo).moviesCountInDbLiveData.observeForever { countInDb ->
            isListAndDbSameSizeLiveData.postValue(repo.getMoviesCountInList() == countInDb)
        }
    }


    fun addToList(movie: Movie) {
        repo.putToDB(listOf(movie))
        moviesListLiveData.postValue(repo.getAllFromDB())
    }

    fun removeFromList(movie: Movie) {
        (repo as MoviesListRepositoryImplWithList).deleteFromDB(movie)
        moviesListLiveData.postValue(repo.getAllFromDB())
    }
}