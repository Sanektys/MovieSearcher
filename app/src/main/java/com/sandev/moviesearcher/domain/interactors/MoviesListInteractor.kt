package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import com.sandev.moviesearcher.domain.Movie
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

    val moviesListLiveData = MutableLiveData<List<Movie>>()

    init {
        moviesListLiveData.postValue(repo.getAllFromDB())
    }


    fun addToList(movie: Movie) {
        repo.putToDB(movie)
        moviesListLiveData.postValue(repo.getAllFromDB())
    }

    fun removeFromList(movie: Movie) {
        (repo as MoviesListRepositoryImplWithList).deleteFromDB(movie)
        moviesListLiveData.postValue(repo.getAllFromDB())
    }

    fun isListAndDbSameSize() =
        (repo as MoviesListRepositoryImplWithList).getMoviesCountInList() == repo.getMoviesCountInDB()


    companion object {
        const val POLL_DELAY = 50L
    }
}