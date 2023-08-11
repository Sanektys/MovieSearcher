package com.example.domain_impl.local_database.di.components

import android.content.Context
import com.example.domain_impl.local_database.di.AllMoviesDatabaseExtendedProvider
import com.example.domain_impl.local_database.di.modules.AllMoviesModule
import com.example.domain_impl.local_database.di.scopes.AllMoviesScope
import dagger.BindsInstance
import dagger.Component


@AllMoviesScope
@Component(modules = [AllMoviesModule::class])
interface AllMoviesDatabaseComponent : AllMoviesDatabaseExtendedProvider {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AllMoviesDatabaseComponent
    }
}