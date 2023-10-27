package com.sandev.moviesearcher.di.modules

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.sandev.moviesearcher.data.RemoteConfigProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class RemoteConfigModule {

    @[Singleton Provides]
    fun provideRemoteConfig(): FirebaseRemoteConfig = RemoteConfigProvider.getInstance()
}