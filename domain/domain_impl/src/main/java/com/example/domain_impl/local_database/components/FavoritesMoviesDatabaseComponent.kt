package com.example.domain_impl.local_database.components

import android.content.Context
import com.example.domain_api.local_database.db_providers.FavoriteDatabaseProvider
import com.example.domain_impl.local_database.modules.FavoriteMoviesModule
import com.example.domain_impl.local_database.scopes.FavoriteMoviesScope
import dagger.BindsInstance
import dagger.Component


@FavoriteMoviesScope
@Component(modules = [FavoriteMoviesModule::class])
interface FavoritesMoviesDatabaseComponent : FavoriteDatabaseProvider {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): FavoritesMoviesDatabaseComponent
    }
}