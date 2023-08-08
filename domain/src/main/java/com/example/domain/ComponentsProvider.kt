package com.example.domain

import com.example.domain_api.the_movie_database.RetrofitProvider
import com.example.domain_impl.the_movie_database.di.components.DaggerRetrofitComponent


fun provideRetrofit(): RetrofitProvider = DaggerRetrofitComponent.builder().build()