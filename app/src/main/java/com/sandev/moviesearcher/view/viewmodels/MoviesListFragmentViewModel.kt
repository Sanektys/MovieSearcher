package com.sandev.moviesearcher.view.viewmodels

import android.view.Gravity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.domain.Interactor
import com.sandev.moviesearcher.domain.Movie


abstract class MoviesListFragmentViewModel : ViewModel() {
    abstract val moviesListLiveData: MutableLiveData<List<Movie>>

    protected abstract val interactor: Interactor

    abstract var lastSearch: CharSequence?

    var lastSlideGravity = Gravity.TOP


    abstract fun searchInDatabase(query: CharSequence): List<Movie>?
    protected fun searchInDatabase(query: CharSequence, source: List<Movie>?): List<Movie>? {
        return source?.filter {
            it.title.lowercase().contains(query.toString().lowercase())
        }
    }


    interface ApiCallback {
        fun onSuccess(movies: List<Movie>)
        fun onFailure()
    }
}