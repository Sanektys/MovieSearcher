package com.sandev.cached_movies_feature.favorite_movies

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain.provideFavoritesMoviesDatabase
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.cached_movies_feature.favorite_movies.di.components.DaggerFavoritesMoviesInteractorComponent
import javax.inject.Inject


class FavoriteMoviesComponentViewModel(context: Context) : ViewModel() {

    private val favoriteMoviesComponent = DaggerFavoritesMoviesInteractorComponent.builder()
        .database(provideFavoritesMoviesDatabase(context))
        .build()

    @Inject
    lateinit var interactor: CachedMoviesInteractor


    init {
        favoriteMoviesComponent.inject(this)
    }


    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(FavoriteMoviesComponentViewModel::class.java)) {
                return FavoriteMoviesComponentViewModel(context) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}