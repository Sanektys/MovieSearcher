package com.sandev.cached_movies_feature.favorite_movies.di.components

import androidx.lifecycle.ViewModel
import com.example.domain_api.local_database.db_providers.FavoriteDatabaseProvider
import com.example.domain_impl.local_database.di.scopes.FavoriteMoviesScope
import com.sandev.cached_movies_feature.favorite_movies.di.FavoritesMoviesInteractorProvider
import com.sandev.cached_movies_feature.favorite_movies.di.modules.FavoritesMoviesInteractorModule
import dagger.Component


@FavoriteMoviesScope
@Component(
    modules = [FavoritesMoviesInteractorModule::class],
    dependencies = [FavoriteDatabaseProvider::class]
)
interface FavoritesMoviesInteractorComponent : FavoritesMoviesInteractorProvider {

    fun inject(viewModel: ViewModel)

    @Component.Builder
    interface Builder {
        fun database(favoriteDatabase: FavoriteDatabaseProvider): Builder
        fun build(): FavoritesMoviesInteractorComponent
    }
}