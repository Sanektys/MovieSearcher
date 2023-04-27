package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.domain.Interactor


class HomeFragmentViewModel : MoviesListFragmentViewModel() {

    override val moviesListLiveData = MutableLiveData<List<Movie>>()

    override val interactor: Interactor = App.instance.interactor

    override var lastSearch: CharSequence?
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch

    companion object {
        private var lastSearch: CharSequence? = null
    }

    init {
        val movies = interactor.getMoviesDB()
        moviesListLiveData.postValue(movies)
    }

    override fun searchInDatabase(query: CharSequence): List<Movie>? {
        return searchInDatabase(query, moviesListLiveData.value)
    }
}