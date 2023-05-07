package com.sandev.moviesearcher

import android.app.Application
import com.sandev.moviesearcher.data.MainRepository
import com.sandev.moviesearcher.domain.Interactor


class App : Application() {
    val repo = MainRepository()
    val interactor = Interactor(repo)

    override fun onCreate() {
        super.onCreate()
        _instance = this
    }

    companion object {
        private var _instance: App? = null
        val instance
            get() = _instance!!
    }
}