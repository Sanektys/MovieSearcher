package com.sandev.tmdb_feature.di

import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor


internal interface TmdbInteractorProvider {

    fun provideInteractor(): TmdbInteractor
}