package com.sandev.moviesearcher

import android.app.Application
import com.sandev.moviesearcher.di.components.AppComponent
import com.sandev.moviesearcher.di.components.DaggerAppComponent


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

    fun getAppComponent() = appComponent ?: DaggerAppComponent.builder()
        .context(this)
        .build().also { appComponent = it }
}