package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.domain.OkHttpBuilder
import com.sandev.moviesearcher.domain.RetrofitBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton


@Module
class RemoteModule {
    @[Provides Singleton]
    fun provideOkHttpClient() = OkHttpBuilder.build()

    @[Provides Singleton]
    fun provideRetrofit(client: OkHttpClient) = RetrofitBuilder.build(client)
}