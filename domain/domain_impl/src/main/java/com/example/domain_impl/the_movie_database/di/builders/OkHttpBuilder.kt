package com.example.domain_impl.the_movie_database.di.builders

import com.example.domain_api.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


object OkHttpBuilder {
    const val CALL_TIMEOUT_IN_SECONDS = 10L
    const val READ_TIMEOUT_IN_SECONDS = 10L

    fun build() = OkHttpClient.Builder()
        .callTimeout(CALL_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()
}