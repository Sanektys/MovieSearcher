package com.sandev.moviesearcher.di.components

import android.content.Context
import com.sandev.moviesearcher.di.modules.NotificationsModule
import com.sandev.moviesearcher.di.modules.SharedPreferenceModule
import com.sandev.moviesearcher.utils.workers.WatchLaterNotificationWorker
import com.sandev.moviesearcher.view.viewmodels.DetailsFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.MainActivityViewModel
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.SettingsFragmentViewModel
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        SharedPreferenceModule::class,
        NotificationsModule::class
    ]
)
interface AppComponent {

    fun inject(moviesListFragmentViewModel: MoviesListFragmentViewModel)
    fun inject(detailsFragmentViewModel: DetailsFragmentViewModel)
    fun inject(settingsFragmentViewModel: SettingsFragmentViewModel)
    fun inject(mainActivityViewModel: MainActivityViewModel)
    fun inject(watchLaterNotificationWorker: WatchLaterNotificationWorker)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(context: Context): Builder
        fun build(): AppComponent
    }
}