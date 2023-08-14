package com.sandev.moviesearcher.view.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.moviesearcher.App
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.IOException
import java.net.URL


class DetailsFragmentViewModel : ViewModel() {

    var favoritesMoviesDatabaseInteractor: CachedMoviesInteractor? = null
        set(value) {
            field = value
            getFavoritesMovies = favoritesMoviesDatabaseInteractor?.getAllFromList()
        }
    var watchLaterMoviesDatabaseInteractor: CachedMoviesInteractor? = null
        set(value) {
            field = value
            getWatchLaterMovies = watchLaterMoviesDatabaseInteractor?.getAllFromList()
        }

    var getFavoritesMovies: Observable<List<DatabaseMovie>>? = null
    var getWatchLaterMovies: Observable<List<DatabaseMovie>>? = null

    var _movie: DatabaseMovie? = null
    val movie: DatabaseMovie
        get() = _movie!!

    var isFavoriteMovie: Boolean = false
    var isWatchLaterMovie: Boolean = false

    var isConfigurationChanged: Boolean = false
    var isLowQualityPosterDownloaded: Boolean = false

    var fragmentThatLaunchedDetails: String? = null


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