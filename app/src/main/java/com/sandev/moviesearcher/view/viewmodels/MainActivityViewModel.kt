package com.sandev.moviesearcher.view.viewmodels

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.view.notifications.WatchMovieNotification
import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.single
import javax.inject.Inject


class MainActivityViewModel(private val tmdbInteractor: TmdbInteractor) : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    @Inject
    lateinit var watchMovieNotification: WatchMovieNotification

    var isPrimaryInitializationPerformed = false

    var navigationBarTranslationY: Float = 0f
    var navigationBarVisibility: Int = View.VISIBLE


    init {
        App.instance.getAppComponent().inject(this)

        watchMovieNotification.registerChannel()
    }


    suspend fun getPromotedMovie(promotionMovieInfo: String): DatabaseMovie? {
        var movieTitle: String? = null
        var movieReleaseYear: String? = null
        promotionMovieInfo.split("&&", limit = 2).forEachIndexed { index, s ->
            when (index) {
                0 -> movieTitle = s.trim()
                1 -> movieReleaseYear = s.trim()
            }
        }

        var promotedMovie: DatabaseMovie? = null

        if (movieTitle != null) {
            if (movieReleaseYear != null) {
                promotedMovie = tmdbInteractor
                    .getSearchedMoviesByYearFromApi(query = movieTitle!!, year = movieReleaseYear!!, page = 1)
                    .single()
                    .movies[0] as DatabaseMovie
            } else {
                var disposable: Disposable? = null
                disposable = tmdbInteractor.getSearchedMoviesFromApi(query = movieTitle!!, page = 1)
                    .subscribe { result ->
                        promotedMovie = result.movies[0] as DatabaseMovie
                        disposable?.dispose()
                    }
            }
        }
        return promotedMovie
    }


    class ViewModelFactory(private val interactor: TmdbInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(interactor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()
}