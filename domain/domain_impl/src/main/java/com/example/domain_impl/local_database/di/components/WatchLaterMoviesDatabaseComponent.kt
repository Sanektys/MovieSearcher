package com.example.domain_impl.local_database.di.components

import android.content.Context
import com.example.domain_api.local_database.db_providers.WatchLaterDatabaseProvider
import com.example.domain_impl.local_database.di.modules.WatchLaterMoviesModule
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import dagger.BindsInstance
import dagger.Component


@WatchLaterMoviesScope
@Component(modules = [WatchLaterMoviesModule::class])
interface WatchLaterMoviesDatabaseComponent : WatchLaterDatabaseProvider {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): WatchLaterMoviesDatabaseComponent
    }
}