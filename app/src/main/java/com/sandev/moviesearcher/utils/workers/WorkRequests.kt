package com.sandev.moviesearcher.utils.workers

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.domain_api.local_database.entities.DatabaseMovie
import java.util.concurrent.TimeUnit


class WorkRequests {

    companion object {

        fun enqueueWatchLaterNotificationWork(
            context: Context,
            movie: DatabaseMovie,
            notificationDate: Long
        ) {
            val workRequest = OneTimeWorkRequestBuilder<WatchLaterNotificationWorker>()
                .setInputData(
                    workDataOf(
                        WatchLaterNotificationWorker.KEY_TARGET_MOVIE_TITLE to movie.title,
                        WatchLaterNotificationWorker.KEY_TARGET_MOVIE_HASH to movie.hashCode()
                    )
                )
                .setInitialDelay(
                    notificationDate - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "${WatchLaterNotificationWorker.WORK_PREFIX}${movie.title}${movie.hashCode()}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }

        fun cancelWatchLaterNotificationWork(context: Context, movie: DatabaseMovie) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(
                    "${WatchLaterNotificationWorker.WORK_PREFIX}${movie.title}${movie.hashCode()}"
                )
        }
    }
}