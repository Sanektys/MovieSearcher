package com.sandev.moviesearcher.di.components

import com.sandev.moviesearcher.di.FavoriteFragmentScope
import com.sandev.moviesearcher.di.modules.FavoriteMoviesListModule
import com.sandev.moviesearcher.domain.components_holders.FavoritesMoviesComponentHolder
import dagger.Component


@FavoriteFragmentScope
@Component(modules = [FavoriteMoviesListModule::class])
interface FavoritesMoviesComponent {

    fun inject(favoritesMoviesComponentViewModel: FavoritesMoviesComponentHolder)

    @Component.Factory
    interface Factory {
        fun create(): FavoritesMoviesComponent
    }
}