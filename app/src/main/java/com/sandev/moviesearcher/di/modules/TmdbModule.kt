package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
class TmdbModule {
    @[Provides Singleton]
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)
}