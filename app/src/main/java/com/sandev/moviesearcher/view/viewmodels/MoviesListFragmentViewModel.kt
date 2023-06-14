package com.sandev.moviesearcher.view.viewmodels

import android.view.Gravity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.data.db.entities.PopularMovie


abstract class MoviesListFragmentViewModel : ViewModel() {

    abstract val moviesListLiveData: MutableLiveData<List<PopularMovie>>

    abstract var lastSearch: String?

    var lastSlideGravity = Gravity.TOP


    abstract fun searchInDatabase(query: CharSequence): List<PopularMovie>?
    protected fun searchInDatabase(query: CharSequence, source: List<PopularMovie>?): List<PopularMovie>? {
        return source?.filter {
            it.title.lowercase().contains(query.toString().lowercase())
        }
    }


    interface ApiCallback {
        fun onSuccess(movies: List<PopularMovie>, totalPages: Int)
        fun onFailure()
    }
}