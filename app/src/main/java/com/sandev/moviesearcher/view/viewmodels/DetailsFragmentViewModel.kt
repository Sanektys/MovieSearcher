package com.sandev.moviesearcher.view.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import java.io.IOException
import java.lang.Exception
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder
    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    val getFavoritesMovies: LiveData<List<Movie>>
    val getWatchLaterMovies: LiveData<List<Movie>>

    @Inject
    lateinit var interactor: TmdbInteractor

    var _movie: Movie? = null
    val movie: Movie
        get() = _movie!!

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


    suspend fun loadMoviePoster(posterUrl: String): Bitmap {
        return suspendCoroutine { continuation ->
            val url = URL(posterUrl)
            try {
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                continuation.resume(bitmap)
            } catch(e: Exception) {
                throw IOException("Connection lost or server didn't respond")
            }
        }
    }

    fun addToFavorite(movie: Movie) = favoritesMoviesComponent.interactor.addToList(movie)

    fun removeFromFavorite(movie: Movie) = favoritesMoviesComponent.interactor.removeFromList(movie)

    fun addToWatchLater(movie: Movie) = watchLaterMoviesComponent.interactor.addToList(movie)

    fun removeFromWatchLater(movie: Movie) = watchLaterMoviesComponent.interactor.removeFromList(movie)
}