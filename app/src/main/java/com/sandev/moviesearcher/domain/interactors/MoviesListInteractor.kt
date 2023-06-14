package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import com.sandev.moviesearcher.data.db.entities.PopularMovie
import java.util.*
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

    val moviesListLiveData = MutableLiveData<List<PopularMovie>>()

    init {
        moviesListLiveData.postValue(repo.getAllFromDB())
    }


    fun addToList(movie: PopularMovie) {
        repo.putToDB(movie)
        moviesListLiveData.postValue(repo.getAllFromDB())
    }

    fun removeFromList(movie: PopularMovie) {
        (repo as MoviesListRepositoryImplWithList).deleteFromDB(movie)
        moviesListLiveData.postValue(repo.getAllFromDB())
    }

    fun isListAndDbSameSize() =
        (repo as MoviesListRepositoryImplWithList).getMoviesCountInList() == repo.getMoviesCountInDB()


    companion object {
        const val POLL_DELAY = 50L
    }
}