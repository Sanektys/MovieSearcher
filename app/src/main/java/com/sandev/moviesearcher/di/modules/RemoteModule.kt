package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.domain.OkHttpBuilder
import com.sandev.moviesearcher.domain.RetrofitBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
class RemoteModule {
    @[Provides Singleton]
    fun provideOkHttpClient(): OkHttpClient = OkHttpBuilder.build()

    @[Provides Singleton]
    fun provideRetrofit(client: OkHttpClient): Retrofit = RetrofitBuilder.build(client)
}