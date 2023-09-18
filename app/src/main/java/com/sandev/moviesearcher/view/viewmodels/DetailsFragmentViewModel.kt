package com.sandev.moviesearcher.view.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.cached_movies_feature.watch_later_movies.domain.WatchLaterMoviesInteractor
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.utils.workers.WorkRequests
import com.sandev.moviesearcher.view.fragments.FavoritesFragment
import com.sandev.moviesearcher.view.fragments.WatchLaterFragment
import com.sandev.moviesearcher.view.notifications.WatchMovieNotification
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.IOException
import java.net.URL
import javax.inject.Inject


class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var watchMovieNotification: WatchMovieNotification

    var favoritesMoviesDatabaseInteractor: CachedMoviesInteractor? = null
        set(value) {
            field = value
            getFavoritesMovies = favoritesMoviesDatabaseInteractor?.getAllFromList()
        }
    var watchLaterMoviesDatabaseInteractor: WatchLaterMoviesInteractor? = null
        set(value) {
            field = value
            getWatchLaterMovies = watchLaterMoviesDatabaseInteractor?.getAllWatchLaterMoviesFromList()
        }

    var getFavoritesMovies: Observable<List<DatabaseMovie>>? = null
    var getWatchLaterMovies: Observable<List<WatchLaterDatabaseMovie>>? = null

    var _movie: DatabaseMovie? = null
    val movie: DatabaseMovie
        get() = _movie!!

    var isFragmentSeparate: Boolean = false

    var isFavoriteMovie: Boolean = false
    var isWatchLaterMovie: Boolean = false

    var isFavoriteButtonSelected: Boolean = false
    var isWatchLaterButtonSelected: Boolean = false

    var isConfigurationChanged: Boolean = false
    var isLowQualityPosterDownloaded: Boolean = false

    var fragmentThatLaunchedDetails: String? = null


    init {
        App.instance.getAppComponent().inject(this)
    }


    fun loadMoviePoster(posterUrl: String): Bitmap {
        val url = URL(posterUrl)

        val bitmap = try {
            val connection = url.openConnection()
            connection.connectTimeout = CONNECTION_TIMEOUT
            connection.readTimeout = CONNECTION_READ_TIMEOUT

            BitmapFactory.decodeStream(connection.getInputStream())
        } catch(e: Exception) {
            throw IOException("Connection lost or server didn't respond")
        }
        return bitmap
    }

    fun changeFavoriteListImmediatelyIfPossible() {
        if (isFavoriteMovie.not() || fragmentThatLaunchedDetails != FavoritesFragment::class.qualifiedName) {
            if (isFavoriteButtonSelected) {
                addToFavorite(movie)
            } else {
                removeFromFavorite(movie)
            }
        }
    }

    fun changeWatchLaterListImmediatelyIfPossible(context: Context, notificationDate: Long?) {
        if (isWatchLaterMovie.not() || fragmentThatLaunchedDetails != WatchLaterFragment::class.qualifiedName) {
            if (isWatchLaterButtonSelected && notificationDate != null) {
                WorkRequests.enqueueWatchLaterNotificationWork(context, movie, notificationDate)

                val watchLaterMovie = WatchLaterDatabaseMovie(movie, notificationDate)
                addToWatchLater(watchLaterMovie)
            } else {
                WorkRequests.cancelWatchLaterNotificationWork(context, movie)

                removeFromWatchLater(movie)
            }
        }
    }

    fun addToFavorite(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = favoritesMoviesDatabaseInteractor?.addToList(databaseMovie)?.subscribe {
            disposable?.dispose()
        }
    }

    fun removeFromFavorite(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = favoritesMoviesDatabaseInteractor?.removeFromList(databaseMovie)?.subscribe {
            disposable?.dispose()
        }
    }

    fun addToWatchLater(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = watchLaterMoviesDatabaseInteractor?.addToList(databaseMovie)?.subscribe {
            disposable?.dispose()
        }
    }

    fun removeFromWatchLater(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = watchLaterMoviesDatabaseInteractor?.removeFromList(databaseMovie)?.subscribe {
            disposable?.dispose()
        }
    }


    companion object {
        const val CONNECTION_TIMEOUT = 2000
        const val CONNECTION_READ_TIMEOUT = 2000
    }
}