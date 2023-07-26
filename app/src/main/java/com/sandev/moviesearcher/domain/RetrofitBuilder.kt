package com.sandev.moviesearcher.domain

import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiConstants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitBuilder {
    fun build(client: OkHttpClient) = Retrofit.Builder()
        .baseUrl(TmdbApiConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.createSynchronous())
        .client(client)
        .build()
}