package com.sandev.tmdb_feature.di.components

import com.example.domain_api.the_movie_database.TmdbRetrofitProvider
import com.example.domain_impl.local_database.di.AllMoviesDatabaseExtendedProvider
import com.example.domain_impl.local_database.di.scopes.AllMoviesScope
import com.sandev.tmdb_feature.TmdbComponentViewModel
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
internal interface TmdbInteractorComponent : TmdbInteractorProvider {

    fun inject(viewModel: TmdbComponentViewModel)

    @Component.Builder
    interface Builder {
        fun retrofit(tmdbRetrofitProvider: TmdbRetrofitProvider): Builder
        fun database(allMoviesDatabaseProvider: AllMoviesDatabaseExtendedProvider): Builder
        fun build(): TmdbInteractorComponent
    }
}