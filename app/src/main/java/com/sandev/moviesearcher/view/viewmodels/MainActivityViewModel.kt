package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import javax.inject.Inject


class MainActivityViewModel : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    var isBatteryCheckedDuringAppStart = false


    init {
        App.instance.getAppComponent().inject(this)
    }


    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()
}