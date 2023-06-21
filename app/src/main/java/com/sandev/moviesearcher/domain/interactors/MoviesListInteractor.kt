package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.LiveData
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplForSavedLists
import java.util.Locale
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()


    fun addToList(movie: Movie) = repo.putToDB(listOf(movie))

    fun removeFromList(movie: Movie) = (repo as MoviesListRepositoryImplForSavedLists).deleteFromDB(movie)

    fun getAllFromList(): LiveData<List<Movie>> = repo.getAllFromDB()
}