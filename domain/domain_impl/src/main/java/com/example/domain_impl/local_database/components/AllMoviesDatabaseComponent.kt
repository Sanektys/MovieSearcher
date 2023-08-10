package com.example.domain_impl.local_database.components

import android.content.Context
import com.example.domain_api.local_database.db_providers.AllMoviesDatabaseProvider
import com.example.domain_impl.local_database.modules.AllMoviesModule
import com.example.domain_impl.local_database.scopes.AllMoviesScope
import dagger.BindsInstance
import dagger.Component


@AllMoviesScope
@Component(modules = [AllMoviesModule::class])
interface AllMoviesDatabaseComponent : AllMoviesDatabaseProvider {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AllMoviesDatabaseComponent
    }
}