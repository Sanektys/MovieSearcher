package com.sandev.moviesearcher.view.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import kotlinx.coroutines.launch
import javax.inject.Inject


class SettingsFragmentViewModel() : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    private val sharedPreferencesCallback = initializeSharedPreferencesCallback()

    private val categoryProperty: MutableLiveData<String> = MutableLiveData()
    val getCategoryProperty: LiveData<String> = categoryProperty

    private val appTheme: MutableLiveData<String> = MutableLiveData()
    val getAppTheme: LiveData<String> = appTheme

    private val appLanguage: MutableLiveData<String> = MutableLiveData()
    val getAppLanguage: LiveData<String> = appLanguage

    private val splashScreenEnabling: MutableLiveData<Boolean> = MutableLiveData()
    val getSplashScreenEnabling: LiveData<Boolean> = splashScreenEnabling

    private val ratingDonutAnimationState: MutableLiveData<Boolean> = MutableLiveData()
    val getRatingDonutAnimationState: LiveData<Boolean> = ratingDonutAnimationState

    private val isSplashScreenButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val getSplashScreenButtonState: LiveData<Boolean> = isSplashScreenButtonEnabled

    private val isRatingDonutButtonEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val getRatingDonutButtonState: LiveData<Boolean> = isRatingDonutButtonEnabled


    init {
        App.instance.getAppComponent().inject(this)

        sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesCallback)

        getCategoryProperty()

        getAppTheme()
        getAppLanguage()

        getSplashScreenButtonState()
        getRatingDonutButtonState()

        getSplashScreenEnabling()
        getRatingDonutAnimationState()
    }


    override fun onCleared() {
        sharedPreferencesInteractor.removeSharedPreferencesChangeListener(sharedPreferencesCallback)
    }

    private fun getCategoryProperty() = viewModelScope.launch {
        categoryProperty.postValue(sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList())
    }

    fun putCategoryProperty(category: String) = viewModelScope.launch {
        sharedPreferencesInteractor.setDefaultMoviesCategoryInMainList(category)
        getCategoryProperty()
    }

    private fun getAppTheme() = appTheme.postValue(sharedPreferencesInteractor.getAppTheme())

    fun setAppTheme(nightMode: String) {
        sharedPreferencesInteractor.setAppTheme(nightMode)
        getAppTheme()
    }

    private fun getAppLanguage() = appLanguage.postValue(sharedPreferencesInteractor.getAppLanguage())

    fun setAppLanguage(language: String) {
        sharedPreferencesInteractor.setAppLanguage(language)
        getAppLanguage()
    }

    private fun getSplashScreenEnabling() = viewModelScope.launch {
        splashScreenEnabling.postValue(sharedPreferencesInteractor.getSplashScreenEnabled())
    }

    fun setSplashScreenEnabling(isEnabled: Boolean) = viewModelScope.launch {
        sharedPreferencesInteractor.setShowingSplashScreen(isEnabled)
        getSplashScreenEnabling()
    }

    private fun getRatingDonutAnimationState()
            = ratingDonutAnimationState.postValue(sharedPreferencesInteractor.isRatingDonutAnimationEnabled())

    fun setRatingDonutAnimationState(isEnabled: Boolean) {
        sharedPreferencesInteractor.setRatingDonutAnimationState(isEnabled)
        getRatingDonutAnimationState()
    }

    private fun getSplashScreenButtonState()
            = isSplashScreenButtonEnabled.postValue(sharedPreferencesInteractor.isSplashScreenSwitchButtonEnabled())

    private fun getRatingDonutButtonState()
            = isRatingDonutButtonEnabled.postValue(sharedPreferencesInteractor.isRatingDonutSwitchButtonEnabled())

    private fun initializeSharedPreferencesCallback()
            = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            SharedPreferencesProvider.KEY_ENABLE_SPLASH_SCREEN_SWITCH_BUTTON -> {
                getSplashScreenEnabling()
                getSplashScreenButtonState()
            }
            SharedPreferencesProvider.KEY_ENABLE_RATING_DONUT_SWITCH_BUTTON -> {
                getRatingDonutAnimationState()
                getRatingDonutButtonState()
            }
            SharedPreferencesProvider.KEY_LANGUAGE -> getAppLanguage()
        }
    }
}