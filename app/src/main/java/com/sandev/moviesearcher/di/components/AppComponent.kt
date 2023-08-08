package com.sandev.moviesearcher.di.components

import android.content.Context
import com.example.domain_api.the_movie_database.RetrofitProvider
import com.sandev.moviesearcher.di.modules.AllMoviesListModule
import com.sandev.moviesearcher.di.modules.RemoteModule
import com.sandev.moviesearcher.di.modules.SharedPreferenceModule
import com.sandev.moviesearcher.di.modules.TmdbInteractorModule
import com.sandev.moviesearcher.di.modules.TmdbModule
import com.sandev.moviesearcher.di.modules.ViewModelModule
import com.sandev.moviesearcher.view.viewmodels.DetailsFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.FavoritesFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.MainActivityViewModel
import com.sandev.moviesearcher.view.viewmodels.SettingsFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.WatchLaterFragmentViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        SharedPreferenceModule::class,
        ViewModelModule::class,
        AllMoviesListModule::class,
    ],
    dependencies = [RetrofitProvider::class]
)
interface AppComponent {

    fun inject(homeFragmentViewModel: HomeFragmentViewModel)
    fun inject(detailsFragmentViewModel: DetailsFragmentViewModel)
    fun inject(favoritesFragmentViewModel: FavoritesFragmentViewModel)
    fun inject(watchLaterFragmentViewModel: WatchLaterFragmentViewModel)
    fun inject(settingsFragmentViewModel: SettingsFragmentViewModel)
    fun inject(mainActivityViewModel: MainActivityViewModel)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun retrofit(retrofitProvider: RetrofitProvider): Builder
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}