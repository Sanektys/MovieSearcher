package com.sandev.moviesearcher.view.viewmodels

import android.view.View
import androidx.lifecycle.ViewModel
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.WatchMovieNotification
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import javax.inject.Inject


class MainActivityViewModel : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    @Inject
    lateinit var watchMovieNotification: WatchMovieNotification

    var isPrimaryInitializationPerformed = false

    var navigationBarTranslationY: Float = 0f
    var navigationBarVisibility: Int = View.VISIBLE


    init {
        App.instance.getAppComponent().inject(this)

        watchMovieNotification.registerChannel()
    }


    fun isSplashScreenEnabled() = sharedPreferencesInteractor.isSplashScreenEnabled()
}