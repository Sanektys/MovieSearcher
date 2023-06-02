package com.sandev.moviesearcher.view.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sandev.moviesearcher.App
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.domain.interactors.SharedPreferencesInteractor
import javax.inject.Inject


class SettingsFragmentViewModel(context: Context) : ViewModel() {

    @Inject
    lateinit var sharedPreferencesInteractor: SharedPreferencesInteractor

    val categoryPropertyLiveData: MutableLiveData<String> = MutableLiveData()

    val categoryPopular = context.getString(R.string.shared_preferences_settings_value_category_popular)
    val categoryTopRated = context.getString(R.string.shared_preferences_settings_value_category_top_rated)
    val categoryUpcoming = context.getString(R.string.shared_preferences_settings_value_category_upcoming)
    val categoryNowPlaying = context.getString(R.string.shared_preferences_settings_value_category_now_playing)

    init {
        App.instance.getAppComponent().inject(this)
        getCategoryProperty()
    }


    private fun getCategoryProperty() {
        categoryPropertyLiveData.value = sharedPreferencesInteractor.getDefaultMoviesCategoryInMainList()
    }

    fun putCategoryProperty(category: String) {
        sharedPreferencesInteractor.setDefaultMoviesCategoryInMainList(category)
        getCategoryProperty()
    }


    class MyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(SettingsFragmentViewModel::class.java)) {
                return SettingsFragmentViewModel(context) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}