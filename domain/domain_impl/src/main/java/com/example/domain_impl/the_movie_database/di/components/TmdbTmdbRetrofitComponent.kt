package com.example.domain_impl.the_movie_database.di.components

import com.example.domain_api.the_movie_database.TmdbRetrofitProvider
import com.example.domain_impl.the_movie_database.di.TmdbRetrofitScope
import com.example.domain_impl.the_movie_database.di.modules.RetrofitModule
import dagger.Component


@TmdbRetrofitScope
@Component(modules = [RetrofitModule::class])
internal interface TmdbTmdbRetrofitComponent : TmdbRetrofitProvider