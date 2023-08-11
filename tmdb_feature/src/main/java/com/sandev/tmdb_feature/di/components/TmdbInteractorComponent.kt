package com.sandev.tmdb_feature.di.components

import androidx.lifecycle.ViewModel
import com.example.domain_api.the_movie_database.TmdbRetrofitProvider
import com.example.domain_impl.local_database.di.AllMoviesDatabaseExtendedProvider
import com.example.domain_impl.local_database.di.scopes.AllMoviesScope
import com.sandev.tmdb_feature.di.TmdbInteractorProvider
import com.sandev.tmdb_feature.di.modules.TmdbInteractorModule
import dagger.Component


@AllMoviesScope
@Component(
    modules = [TmdbInteractorModule::class],
    dependencies = [
        TmdbRetrofitProvider::class,
        AllMoviesDatabaseExtendedProvider::class,
    ]
)
interface TmdbInteractorComponent : TmdbInteractorProvider {

    fun inject(viewModel: ViewModel)

    @Component.Builder
    interface Builder {
        fun retrofit(tmdbRetrofitProvider: TmdbRetrofitProvider): Builder
        fun database(allMoviesDatabaseProvider: AllMoviesDatabaseExtendedProvider): Builder
        fun build(): TmdbInteractorComponent
    }
}