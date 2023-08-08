package com.example.domain_impl.the_movie_database.di.components

import com.example.domain_api.the_movie_database.RetrofitProvider
import com.example.domain_impl.the_movie_database.di.RetrofitScope
import com.example.domain_impl.the_movie_database.di.modules.RetrofitModule
import com.example.domain_impl.the_movie_database.di.modules.TmdbInteractorModule
import dagger.Component


@RetrofitScope
@Component(modules = [RetrofitModule::class, TmdbInteractorModule::class])
internal interface RetrofitComponent : RetrofitProvider