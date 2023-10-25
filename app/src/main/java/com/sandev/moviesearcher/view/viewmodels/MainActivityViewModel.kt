package com.sandev.moviesearcher.view.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import com.sandev.moviesearcher.view.notifications.WatchMovieNotification
import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivityViewModel(private val tmdbInteractor: TmdbInteractor) : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    @Inject
    lateinit var watchMovieNotification: WatchMovieNotification

    @Inject
    lateinit var remoteConfig: FirebaseRemoteConfig

    var isPrimaryInitializationPerformed = false

    var navigationBarTranslationY: Float = 0f
    var navigationBarVisibility: Int = View.VISIBLE

    private var promotedMovie: DatabaseMovie? = null
        set(value) {
            field = value
            promotedMovieLiveData.postValue(value)
            if (value != null) isPromotedMovieShowed = false
        }
    var isPromotedMovieShowed: Boolean = false
        set(value) {
            field = value
            if (value) promotedMovie = null
        }

    private val promotedMovieLiveData = MutableLiveData<DatabaseMovie?>()
    val getPromotedMovie: LiveData<DatabaseMovie?> = promotedMovieLiveData


    init {
        App.instance.getAppComponent().inject(this)

        watchMovieNotification.registerChannel()
    }


    suspend fun checkForCurrentMoviePromotion() {
        val previouslyPromotedMovie = remoteConfig.getString(KEY_PROMOTION_MOVIE)

        remoteConfig.fetchAndActivate().await().let { isNewValueActivated ->
            if (isNewValueActivated) {
                val currentPromotedMovie = remoteConfig.getString(KEY_PROMOTION_MOVIE)

                if (previouslyPromotedMovie != currentPromotedMovie) {
                    getPromotedMovie(currentPromotedMovie)
                }
            }
        }
    }

    private suspend fun getPromotedMovie(promotionMovieInfo: String) {
        var movieTitle: String? = null
        var movieReleaseYear: String? = null
        promotionMovieInfo.split("&&", limit = 2).forEachIndexed { index, s ->
            when (index) {
                0 -> movieTitle = s.trim()
                1 -> movieReleaseYear = s.trim()
            }
        }

        if (movieTitle != null) {
            if (movieReleaseYear != null) {
                promotedMovie = try {
                    tmdbInteractor
                        .getSearchedMoviesByYearFromApi(
                            query = movieTitle!!,
                            year = movieReleaseYear!!,
                            page = 1
                        )
                        .single()
                        .movies[0] as DatabaseMovie
                } catch (e: Exception) {
                    null
                }
            } else {
                promotedMovie = suspendCoroutine { continuation ->
                    var disposable: Disposable? = null
                    disposable = tmdbInteractor.getSearchedMoviesFromApi(query = movieTitle!!, page = 1)
                        .subscribe(
                            onSuccess@{ result ->
                                disposable?.dispose()
                                continuation.resume(result.movies[0] as DatabaseMovie)
                            },
                            onError@{
                                disposable?.dispose()
                                continuation.resume(null)
                            }
                        )
                }
            }
        }
    }

    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()


    class ViewModelFactory(private val interactor: TmdbInteractor) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(interactor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    companion object {
        private const val KEY_PROMOTION_MOVIE = "promotingMovie"
    }
}