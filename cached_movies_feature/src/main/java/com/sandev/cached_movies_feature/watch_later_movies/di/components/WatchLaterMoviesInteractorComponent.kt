package com.sandev.cached_movies_feature.watch_later_movies.di.components

import androidx.lifecycle.ViewModel
import com.example.domain_api.local_database.db_providers.WatchLaterDatabaseProvider
import com.example.domain_impl.local_database.di.scopes.WatchLaterMoviesScope
import com.sandev.cached_movies_feature.watch_later_movies.di.WatchLaterMoviesInteractorProvider
import com.sandev.cached_movies_feature.watch_later_movies.di.modules.WatchLaterMoviesInteractorModule
import dagger.Component


@WatchLaterMoviesScope
@Component(
    modules = [WatchLaterMoviesInteractorModule::class],
    dependencies = [WatchLaterDatabaseProvider::class]
)
interface WatchLaterMoviesInteractorComponent : WatchLaterMoviesInteractorProvider {

    fun inject(viewModel: ViewModel)

    @Component.Builder
    interface Builder {
        fun database(watchLaterDatabase: WatchLaterDatabaseProvider): Builder
        fun build(): WatchLaterMoviesInteractorComponent
    }
}