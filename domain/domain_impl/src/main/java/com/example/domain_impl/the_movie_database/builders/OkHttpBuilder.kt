package com.example.domain_impl.the_movie_database.builders

import com.example.domain_api.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


internal object OkHttpBuilder {
    private const val CALL_TIMEOUT_IN_SECONDS = 10L
    private const val READ_TIMEOUT_IN_SECONDS = 10L

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