package com.sandev.moviesearcher.di

import com.sandev.moviesearcher.di.modules.RemoteModule
import com.sandev.moviesearcher.di.modules.RepositoryModule
import com.sandev.moviesearcher.di.modules.TmdbModule
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [
    RemoteModule::class,
    RepositoryModule::class,
    TmdbModule::class
])
interface AppComponent {

    fun inject(homeFragmentViewModel: HomeFragmentViewModel)

    @Component.Builder
    interface Builder {
        fun build(): AppComponent
    }
}