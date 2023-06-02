package com.sandev.moviesearcher.di.modules

import android.content.Context
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class SharedPreferenceModule {

    @[Singleton Provides]
    fun provideSharedPreferences(context: Context): SharedPreferencesProvider = SharedPreferencesProvider(context)

    @[Singleton Provides]
    fun provideSharedPreferencesInteractor(sharedPreferencesProvider: SharedPreferencesProvider): SharedPreferencesInteractor
            = SharedPreferencesInteractor(sharedPreferencesProvider)
}