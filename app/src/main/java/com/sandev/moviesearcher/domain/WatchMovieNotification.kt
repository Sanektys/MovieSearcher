package com.sandev.moviesearcher.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.view.MainActivity


class WatchMovieNotification(private val context: Context) {

    fun registerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_watch_movie_channel_name)
            val channelImportance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance)
            channel.description = context.getString(R.string.notification_watch_movie_channel_description)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify(movie: DatabaseMovie) {
        val watchMovieIntent = Intent(context, MainActivity::class.java)
        watchMovieIntent.putExtra(MainActivity.MOVIE_DATA_KEY, movie)
        val pendingIntent = PendingIntent.getActivity(context, PENDING_INTENT_WATCH_MOVIE, watchMovieIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_watch_movie_title))
                .setContentText(context.getString(R.string.notification_watch_movie_description, movie.title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.notification_watch_movie_description, movie.title)))
                .setSmallIcon(R.drawable.movie_searcher_action_icon)
                .setColor(context.getColor(R.color.md_theme_primaryContainer))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            Notification.Builder(context)
                .setContentTitle(context.getString(R.string.notification_watch_movie_title))
                .setContentText(context.getString(R.string.notification_watch_movie_description, movie.title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.notification_watch_movie_description, movie.title)))
                .setSmallIcon(R.drawable.movie_searcher_action_icon)
                .setColor(context.getColor(R.color.md_theme_primaryContainer))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_LOW)
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            notify(NOTIFICATION_ID, notification.build())
        }
    }


    companion object {
        const val CHANNEL_ID = "watch_movie_channel"

        private const val PENDING_INTENT_WATCH_MOVIE = 1
        private const val NOTIFICATION_ID = 1
    }
}