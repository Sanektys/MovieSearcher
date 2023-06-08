package com.sandev.moviesearcher.di.modules

import android.content.Context
import com.sandev.moviesearcher.data.db.WatchLaterMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplWithList
import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import dagger.Binds
import dagger.Module
import dagger.Provides


@Module(includes = [WatchLaterMoviesListRepositoryModule::class])
class WatchLaterMoviesListModule {

    @[Provides WatchLaterFragmentScope]
    fun provideWatchLaterMoviesDatabase(context: Context): WatchLaterMoviesDatabase
            = WatchLaterMoviesDatabase(context)

    @[Provides WatchLaterFragmentScope]
    fun provideWatchLaterMoviesRepository(moviesDatabase: WatchLaterMoviesDatabase) : MoviesListRepositoryImplWithList
            = MoviesListRepositoryImplWithList(moviesDatabase)
}

@Module
interface WatchLaterMoviesListRepositoryModule {
    @[Binds WatchLaterFragmentScope]
    fun bindFavoriteMoviesList(moviesListRepository: MoviesListRepositoryImplWithList): MoviesListRepository
}