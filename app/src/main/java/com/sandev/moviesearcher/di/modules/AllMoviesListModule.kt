package com.sandev.moviesearcher.di.modules

import android.content.Context
import com.sandev.moviesearcher.data.db.AllMoviesDatabase
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module(includes = [AllMoviesListRepositoryModule::class])
class AllMoviesListModule {

    @[Provides Singleton]
    fun provideAllMoviesDatabase(context: Context): AllMoviesDatabase = AllMoviesDatabase(context)

    @[Provides Singleton]
    fun provideAllMoviesListRepository(moviesDatabase: AllMoviesDatabase): MoviesListRepositoryImpl
            = MoviesListRepositoryImpl(moviesDatabase)
}

@Module
interface AllMoviesListRepositoryModule {

    @[Binds Singleton]
    fun bindMoviesListRepository(allMoviesListRepository: MoviesListRepositoryImpl): MoviesListRepository
}