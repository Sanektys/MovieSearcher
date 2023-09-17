package com.sandev.moviesearcher.view.notifications

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.view.MainActivity


class WatchMovieNotification(private val context: Context) {

    fun registerChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.notification_watch_movie_channel_name)
            val channelImportance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, channelImportance)
            channel.description = context.getString(R.string.notification_watch_movie_channel_description)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun notify(movie: DatabaseMovie) {
        val watchMovieIntent = Intent(context, MainActivity::class.java)
        watchMovieIntent.putExtra(MainActivity.MOVIE_DATA_KEY, movie)
        val pendingIntent = PendingIntent.getActivity(
            context,
            movie.hashCode(),
            watchMovieIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
                .setContentTitle(context.getString(R.string.notification_watch_movie_title))
                .setContentText(context.getString(R.string.notification_watch_movie_description, movie.title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.notification_watch_movie_description, movie.title)))
                .setSmallIcon(R.drawable.movie_searcher_action_icon)
                .setColor(context.getColor(R.color.md_theme_primary))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            Notification.Builder(context)
                .setContentTitle(context.getString(R.string.notification_watch_movie_title))
                .setContentText(context.getString(R.string.notification_watch_movie_description, movie.title))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.notification_watch_movie_description, movie.title)))
                .setSmallIcon(R.drawable.movie_searcher_action_icon)
                .setColor(context.getColor(R.color.md_theme_primary))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_DEFAULT)
        }

        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            notify(movie.hashCode(), notification.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun openChannelSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
        }
        activity.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIsChannelEnabled(): Boolean {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
            return if (areNotificationsEnabled()) {
                getNotificationChannel(CHANNEL_ID).importance != NotificationManager.IMPORTANCE_NONE
            } else false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIsChannelEnabledWithSnackbar(activity: Activity, anchorView: View): Boolean {
        val isChannelEnabled = checkIsChannelEnabled()
        if (isChannelEnabled.not()) {
            Snackbar.make(
                activity,
                anchorView,
                activity.getString(R.string.details_fragment_fab_add_watch_later_notifications_disabled_message),
                Snackbar.LENGTH_LONG
            ).setAction(activity.getString(R.string.details_fragment_fab_add_watch_later_notifications_disabled_button)) {
                openChannelSettings(activity)
            }.show()
        }
        return isChannelEnabled
    }


    companion object {
        const val CHANNEL_ID = "watch_movie_channel"
    }
}