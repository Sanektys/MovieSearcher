package com.example.domain_impl.the_movie_database.builders

import com.example.domain_impl.the_movie_database.constants.TmdbConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


internal fun retrofitBuilder(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(TmdbConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
        .client(client)
        .build()