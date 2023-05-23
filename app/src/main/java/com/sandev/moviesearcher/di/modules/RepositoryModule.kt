package com.sandev.moviesearcher.di.modules

import com.sandev.moviesearcher.data.MainRepository
import com.sandev.moviesearcher.data.Repository
import dagger.Binds
import dagger.Module
import javax.inject.Singleton


@Module
interface RepositoryModule {
    @[Binds Singleton]
    fun bindRepository(repository: MainRepository): Repository
}