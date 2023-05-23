package com.sandev.moviesearcher

import android.app.Application
import com.sandev.moviesearcher.di.AppComponent
import com.sandev.moviesearcher.di.DaggerAppComponent


class App : Application() {
    private var appComponent: AppComponent? = null

    companion object {
        private var _instance: App? = null
        val instance
            get() = _instance!!
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this

        getAppComponent()
    }

    fun getAppComponent() = appComponent ?: DaggerAppComponent.builder().build().also { appComponent = it }
}