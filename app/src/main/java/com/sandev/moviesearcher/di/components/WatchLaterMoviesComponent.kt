package com.sandev.moviesearcher.di.components

import android.content.Context
import com.sandev.moviesearcher.di.WatchLaterFragmentScope
import com.sandev.moviesearcher.di.modules.WatchLaterMoviesListModule
import com.sandev.moviesearcher.domain.components_holders.WatchLaterMoviesComponentHolder
import dagger.BindsInstance
import dagger.Component


@WatchLaterFragmentScope
@Component(modules = [WatchLaterMoviesListModule::class])
interface WatchLaterMoviesComponent {

    fun inject(watchLaterMoviesComponentViewModel: WatchLaterMoviesComponentHolder)

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): WatchLaterMoviesComponent
    }
}