package com.sandev.moviesearcher.view.viewmodels

import android.view.Gravity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter


abstract class MoviesListFragmentViewModel : ViewModel() {

    abstract val moviesListLiveData: LiveData<List<Movie>>

    protected abstract val moviesObserver: Observer<List<Movie>>

    abstract var lastSearch: String
        protected set

    protected var moviesDatabase: List<Movie> = emptyList()
        set(value) {
            field = value
            initializeRecyclerAdapterList()
        }

    val recyclerAdapter: MoviesRecyclerAdapter = MoviesRecyclerAdapter()

    var lastSlideGravity = Gravity.TOP


    abstract fun searchInDatabase(query: CharSequence): List<Movie>?
    protected fun searchInDatabase(query: CharSequence, source: List<Movie>?): List<Movie>? {
        return source?.filter {
            it.title.lowercase().contains(query.toString().lowercase())
        }
    }

    open fun getAllMovies(): List<Movie>? = moviesListLiveData.value

    open fun initializeRecyclerAdapterList() {
        // Загрузить в recycler результат по прошлому запросу в поиск
        searchInSearchView(lastSearch)
    }

    open fun searchInSearchView(query: String) {
        if (query.length >= SEARCH_SYMBOLS_THRESHOLD) {
            recyclerAdapter.setList(searchInDatabase(query))
        } else {
            recyclerAdapter.setList(getAllMovies())
        }
        lastSearch = query
    }


    interface ApiCallback {
        fun onSuccess(moviesPerPage: Int, totalPages: Int)
        fun onFailure()
    }


    companion object {
        const val SEARCH_SYMBOLS_THRESHOLD = 2
    }
}