package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MainActivityViewModel : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor


    init {
        App.instance.getAppComponent().inject(this)
    }


    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()
}