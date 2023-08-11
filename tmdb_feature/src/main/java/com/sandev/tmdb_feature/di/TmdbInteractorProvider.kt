package com.sandev.tmdb_feature.di

import com.sandev.tmdb_feature.domain.interactors.TmdbInteractor


interface TmdbInteractorProvider {

    fun provideInteractor(): TmdbInteractor
}