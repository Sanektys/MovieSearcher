package com.example.domain

import android.content.Context
import com.example.domain_api.local_database.db_providers.FavoriteDatabaseProvider
import com.example.domain_api.local_database.db_providers.WatchLaterDatabaseProvider
import com.example.domain_api.the_movie_database.TmdbRetrofitProvider
import com.example.domain_impl.local_database.di.AllMoviesDatabaseExtendedProvider
import com.example.domain_impl.local_database.di.components.DaggerAllMoviesDatabaseComponent
import com.example.domain_impl.local_database.di.components.DaggerFavoritesMoviesDatabaseComponent
import com.example.domain_impl.local_database.di.components.DaggerWatchLaterMoviesDatabaseComponent
import com.example.domain_impl.the_movie_database.di.components.DaggerTmdbRetrofitComponent


fun provideRetrofit(): TmdbRetrofitProvider = DaggerTmdbRetrofitComponent.builder().build()

fun provideAllMoviesDatabase(context: Context): AllMoviesDatabaseExtendedProvider
        = DaggerAllMoviesDatabaseComponent.factory().create(context)

fun provideFavoritesMoviesDatabase(context: Context): FavoriteDatabaseProvider
        = DaggerFavoritesMoviesDatabaseComponent.factory().create(context)

fun provideWatchLaterMoviesDatabase(context: Context): WatchLaterDatabaseProvider
        = DaggerWatchLaterMoviesDatabaseComponent.factory().create(context)