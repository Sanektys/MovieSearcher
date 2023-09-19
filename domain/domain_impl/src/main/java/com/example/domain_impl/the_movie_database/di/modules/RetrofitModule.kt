package com.example.domain_impl.the_movie_database.di.modules

import com.example.domain_api.the_movie_database.api.TmdbApi
import com.example.domain_impl.the_movie_database.builders.OkHttpBuilder
import com.example.domain_impl.the_movie_database.builders.retrofitBuilder
import com.example.domain_impl.the_movie_database.di.TmdbRetrofitScope
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit


@Module
class RetrofitModule {

    @[Provides TmdbRetrofitScope]
    fun provideOkHttp(): OkHttpClient = OkHttpBuilder.build()

    @[Provides TmdbRetrofitScope]
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofitBuilder(client)

    @[Provides TmdbRetrofitScope]
    fun provideApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)
}