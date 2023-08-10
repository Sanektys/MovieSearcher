package com.example.domain

import com.example.domain_api.the_movie_database.TmdbRetrofitProvider
import com.example.domain_impl.the_movie_database.di.components.DaggerTmdbRetrofitComponent


fun provideRetrofit(): TmdbRetrofitProvider = DaggerTmdbRetrofitComponent.builder().build()