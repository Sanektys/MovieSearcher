package com.sandev.moviesearcher.di.components

import com.sandev.moviesearcher.di.FavoriteFragmentScope
import com.sandev.moviesearcher.view.viewmodels.FavoritesMoviesComponentViewModel
import dagger.Component


@FavoriteFragmentScope
@Component(dependencies = [AppComponent::class])
interface FavoritesMoviesComponent {

    fun inject(favoritesMoviesComponentViewModel: FavoritesMoviesComponentViewModel)

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): FavoritesMoviesComponent
    }
}