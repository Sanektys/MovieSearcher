package com.sandev.moviesearcher.domain.components_holders

import com.sandev.moviesearcher.domain.interactors.MoviesListInteractor


interface SavedMoviesComponentHolder {
    var interactor: MoviesListInteractor
}