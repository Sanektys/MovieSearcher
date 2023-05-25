package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class ViewModelModule {

    @[Provides Singleton]
    fun provideFavoritesMoviesComponentViewModel() = FavoritesMoviesComponentHolder()

    @[Provides Singleton]
    fun provideWatchLaterMoviesComponentViewModel() = WatchLaterMoviesComponentHolder()
}