package com.sandev.moviesearcher.utils.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.cached_movies_feature.watch_later_movies.WatchLaterMoviesComponentViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.view.notifications.WatchMovieNotification
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject


class WatchLaterNotificationWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    @Inject
    lateinit var watchMovieNotification: WatchMovieNotification

    init {
        App.instance.getAppComponent().inject(this)
    }

    override fun doWork(): Result {
        val targetMovieTitle = inputData.getString(KEY_TARGET_MOVIE_TITLE) ?: return Result.failure()
        val targetMovieHash  = inputData.getInt(KEY_TARGET_MOVIE_HASH, 0)

        val watchLaterMoviesDatabase = WatchLaterMoviesComponentViewModel(applicationContext)
        var moviesWithSameTitle: List<DatabaseMovie>? = null

        watchLaterMoviesDatabase.interactor
            .getFewSearchedWatchLaterMoviesFromList(
                query = targetMovieTitle,
                from = 0,
                moviesCount = Int.MAX_VALUE)
            .blockingSubscribe { foundMovieList ->
                moviesWithSameTitle = foundMovieList
            }

        if (moviesWithSameTitle == null || moviesWithSameTitle!!.isEmpty()) return Result.failure()

        val movie = moviesWithSameTitle?.first { targetMovie ->
            targetMovie.hashCode() == targetMovieHash
        } ?: return Result.failure()

        watchMovieNotification.notify(movie)
        var disposable: Disposable? = null
        disposable = watchLaterMoviesDatabase.interactor.removeFromList(movie).subscribe {
            disposable?.dispose()
        }

        return Result.success()
    }


    companion object {
        const val KEY_TARGET_MOVIE_TITLE = "target_movie_title"
        const val KEY_TARGET_MOVIE_HASH  = "target_movie_hash"

        const val WORK_PREFIX = "WLNotificationWork"
    }
}