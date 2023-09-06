package com.sandev.moviesearcher.di.modules

import android.content.Context
import com.sandev.moviesearcher.view.notifications.WatchMovieNotification
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class NotificationsModule {

    @[Singleton Provides]
    fun provideWatchMovieNotification(context: Context): WatchMovieNotification = WatchMovieNotification(context)
}