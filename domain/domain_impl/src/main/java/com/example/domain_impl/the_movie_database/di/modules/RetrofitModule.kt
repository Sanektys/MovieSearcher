package com.example.domain_impl.the_movie_database.di.modules

import com.example.domain_api.the_movie_database.api.TmdbApi
import com.example.domain_impl.the_movie_database.di.RetrofitScope
import com.example.domain_impl.the_movie_database.di.builders.OkHttpBuilder
import com.example.domain_impl.the_movie_database.di.builders.retrofitBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit


@Module
class RetrofitModule {

    @[Provides RetrofitScope]
    fun provideOkHttp(): OkHttpClient = OkHttpBuilder.build()

    @[Provides RetrofitScope]
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofitBuilder(client)

    @[Provides RetrofitScope]
    fun provideApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)
}