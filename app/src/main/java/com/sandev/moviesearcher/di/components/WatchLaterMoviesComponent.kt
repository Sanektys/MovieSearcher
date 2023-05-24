package com.sandev.moviesearcher.di.components

import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import com.sandev.moviesearcher.view.viewmodels.WatchLaterMoviesComponentViewModel
import dagger.Component


@WatchLaterFragmentScope
@Component(dependencies = [AppComponent::class])
interface WatchLaterMoviesComponent {

    fun inject(watchLaterMoviesComponentViewModel: WatchLaterMoviesComponentViewModel)

    @Component.Factory
    interface Factory {
        fun create(appComponent: AppComponent): WatchLaterMoviesComponent
    }
}