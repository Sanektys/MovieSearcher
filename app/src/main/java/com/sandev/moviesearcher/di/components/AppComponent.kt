package com.sandev.moviesearcher.di.components

import android.content.Context
import com.sandev.moviesearcher.di.modules.RemoteModule
import com.sandev.moviesearcher.di.modules.SharedPreferenceModule
import com.sandev.moviesearcher.di.modules.TmdbModule
import com.sandev.moviesearcher.di.modules.ViewModelModule
import com.sandev.moviesearcher.view.viewmodels.DetailsFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.FavoritesFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.WatchLaterFragmentViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [
    SharedPreferenceModule::class,
    RemoteModule::class,
    TmdbModule::class,
    ViewModelModule::class,
])
interface AppComponent {

    fun inject(homeFragmentViewModel: HomeFragmentViewModel)
    fun inject(detailsFragmentViewModel: DetailsFragmentViewModel)
    fun inject(favoritesFragmentViewModel: FavoritesFragmentViewModel)
    fun inject(watchLaterFragmentViewModel: WatchLaterFragmentViewModel)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}