package com.sandev.moviesearcher.di.components

import android.content.Context
import com.sandev.moviesearcher.di.modules.SharedPreferenceModule
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
    modules = [SharedPreferenceModule::class]
)
interface AppComponent {

    fun inject(homeFragmentViewModel: HomeFragmentViewModel)
    fun inject(settingsFragmentViewModel: SettingsFragmentViewModel)
    fun inject(mainActivityViewModel: MainActivityViewModel)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}