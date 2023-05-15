package com.sandev.moviesearcher.di

import com.sandev.moviesearcher.BuildConfig
import com.sandev.moviesearcher.data.MainRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiConstants
import com.sandev.moviesearcher.domain.Interactor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DI {
    private const val CALL_TIMEOUT_IN_SECONDS = 10L
    private const val READ_TIMEOUT_IN_SECONDS = 10L

    @Singleton
    @Provides
    fun provideMainRepository() = MainRepository()

    @Singleton
    @Provides
    fun provideTmdbApi(): TmdbApi {
        val client = OkHttpClient.Builder()
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

        val retrofit = Retrofit.Builder()
            .baseUrl(TmdbApiConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(TmdbApi::class.java)
    }

    @Singleton
    @Provides
    fun provideInteractor(repo: MainRepository, tmdbApi: TmdbApi) = Interactor(repo, tmdbApi)
}