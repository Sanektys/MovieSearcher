package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.view.viewmodels.FavoritesMoviesComponentViewModel
import com.sandev.moviesearcher.view.viewmodels.WatchLaterMoviesComponentViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class ViewModelModule {

    @[Provides Singleton]
    fun provideFavoritesMoviesComponentViewModel() = FavoritesMoviesComponentViewModel()

    @[Provides Singleton]
    fun provideWatchLaterMoviesComponentViewModel() = WatchLaterMoviesComponentViewModel()
}