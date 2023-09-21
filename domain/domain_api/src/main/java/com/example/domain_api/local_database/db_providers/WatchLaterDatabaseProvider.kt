package com.example.domain_api.local_database.db_providers

import com.example.domain_api.local_database.repository.WatchLaterListRepository


interface WatchLaterDatabaseProvider {

    fun provideRepository(): WatchLaterListRepository
}