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


    init {
        App.instance.getAppComponent().inject(this)
        getCategoryProperty()
    }


    private fun getCategoryProperty() = viewModelScope.launch {
        categoryProperty.postValue(sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList())
    }

    fun putCategoryProperty(category: String) = viewModelScope.launch {
        sharedPreferencesInteractor.setDefaultMoviesCategoryInMainList(category)
        getCategoryProperty()
    }
}