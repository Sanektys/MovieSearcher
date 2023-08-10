package com.sandev.moviesearcher.view.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.io.IOException
import java.net.URL
import javax.inject.Inject


class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder
    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    val getFavoritesMovies: Observable<List<DatabaseMovie>>
    val getWatchLaterMovies: Observable<List<DatabaseMovie>>

    @Inject
    lateinit var interactor: TmdbInteractor

    var _Database_movie: DatabaseMovie? = null
    val databaseMovie: DatabaseMovie
        get() = _Database_movie!!

    var isFavoriteMovie: Boolean = false
    var isWatchLaterMovie: Boolean = false

    var isConfigurationChanged: Boolean = false
    var isLowQualityPosterDownloaded: Boolean = false

    var fragmentThatLaunchedDetails: String? = null

    init {
        App.instance.getAppComponent().inject(this)

        getFavoritesMovies = favoritesMoviesComponent.interactor.getAllFromList()
        getWatchLaterMovies = watchLaterMoviesComponent.interactor.getAllFromList()
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

    fun addToFavorite(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = favoritesMoviesComponent.interactor.addToList(databaseMovie).subscribe {
            disposable?.dispose()
        }
    }

    fun removeFromFavorite(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = favoritesMoviesComponent.interactor.removeFromList(databaseMovie).subscribe {
            disposable?.dispose()
        }
    }

    fun addToWatchLater(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = watchLaterMoviesComponent.interactor.addToList(databaseMovie).subscribe {
            disposable?.dispose()
        }
    }

    fun removeFromWatchLater(databaseMovie: DatabaseMovie) {
        var disposable: Disposable? = null
        disposable = watchLaterMoviesComponent.interactor.removeFromList(databaseMovie).subscribe {
            disposable?.dispose()
        }
    }


    companion object {
        const val CONNECTION_TIMEOUT = 2000
        const val CONNECTION_READ_TIMEOUT = 2000
    }
}