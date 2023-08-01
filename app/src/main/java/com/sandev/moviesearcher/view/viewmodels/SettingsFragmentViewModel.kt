package com.sandev.moviesearcher.view.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject


class SettingsFragmentViewModel() : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    private val categoryProperty: MutableLiveData<String> = MutableLiveData()
    val getCategoryProperty: LiveData<String> = categoryProperty

    private val splashScreenEnabling: MutableLiveData<Boolean> = MutableLiveData()
    val getSplashScreenEnabling: LiveData<Boolean> = splashScreenEnabling


    init {
        App.instance.getAppComponent().inject(this)

        getCategoryProperty()
        getSplashScreenEnabling()
    }


    private fun getCategoryProperty() = viewModelScope.launch {
        categoryProperty.postValue(sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList())
    }

    fun putCategoryProperty(category: String) = viewModelScope.launch {
        sharedPreferencesInteractor.setDefaultMoviesCategoryInMainList(category)
        getCategoryProperty()
    }

    private fun getSplashScreenEnabling() = viewModelScope.launch {
        splashScreenEnabling.postValue(sharedPreferencesInteractor.getSplashScreenEnabled())
    }

    fun setSplashScreenEnabling(isEnabled: Boolean) = viewModelScope.launch {
        sharedPreferencesInteractor.setShowingSplashScreen(isEnabled)
        getSplashScreenEnabling()
    }
}