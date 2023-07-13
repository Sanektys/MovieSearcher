package com.sandev.moviesearcher.view.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import com.sandev.moviesearcher.domain.interactors.TmdbInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class DetailsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var favoritesMoviesComponent: FavoritesMoviesComponentHolder
    @Inject
    lateinit var watchLaterMoviesComponent: WatchLaterMoviesComponentHolder

    var getFavoritesMovies: LiveData<List<Movie>>? = null
        private set
    var getWatchLaterMovies: LiveData<List<Movie>>? = null
        private set

    val favoritesMoviesObtainSynchronizeBlock = Channel<Nothing>()
    val watchLaterMoviesObtainSynchronizeBlock = Channel<Nothing>()

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

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch {
                    getFavoritesMovies = favoritesMoviesComponent.interactor.getAllFromList()
                    favoritesMoviesObtainSynchronizeBlock.close()
                }
                launch {
                    getWatchLaterMovies = watchLaterMoviesComponent.interactor.getAllFromList()
                    watchLaterMoviesObtainSynchronizeBlock.close()
                }
            }
        }
    }


    suspend fun loadMoviePoster(posterUrl: String): Bitmap {
        return suspendCoroutine { continuation ->
            val url = URL(posterUrl)

            val bitmap = try {
                val connection = url.openConnection()
                connection.connectTimeout = CONNECTION_TIMEOUT
                connection.readTimeout = CONNECTION_READ_TIMEOUT

                BitmapFactory.decodeStream(connection.getInputStream())
            } catch(e: Exception) {
                throw IOException("Connection lost or server didn't respond")
            }
            continuation.resume(bitmap)
        }
    }

    fun addToFavorite(movie: Movie) = viewModelScope.launch {
        favoritesMoviesComponent.interactor.addToList(movie)
    }

    fun removeFromFavorite(movie: Movie) = viewModelScope.launch {
        favoritesMoviesComponent.interactor.removeFromList(movie)
    }

    fun addToWatchLater(movie: Movie) = viewModelScope.launch {
        watchLaterMoviesComponent.interactor.addToList(movie)
    }

    fun removeFromWatchLater(movie: Movie) = viewModelScope.launch {
        watchLaterMoviesComponent.interactor.removeFromList(movie)
    }


    companion object {
        const val CONNECTION_TIMEOUT = 2000
        const val CONNECTION_READ_TIMEOUT = 2000
    }
}