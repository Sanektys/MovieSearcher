package com.sandev.moviesearcher

import android.app.Application
import com.sandev.moviesearcher.data.MainRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiConstants
import com.sandev.moviesearcher.domain.Interactor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class App : Application() {

    val repo = MainRepository()
    var _interactor: Interactor? = null
    val interactor: Interactor
        get() = _interactor!!

    companion object {
        private var _instance: App? = null
        val instance
            get() = _instance!!

        const val CALL_TIMEOUT_IN_SECONDS = 10L
        const val READ_TIMEOUT_IN_SECONDS = 10L
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this

        val client = OkHttpClient.Builder()
            .callTimeout(CALL_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply{
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BASIC
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(TmdbApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val retrofitService = retrofit.create(TmdbApi::class.java)

        _interactor = Interactor(repo, retrofitService)
    }
}