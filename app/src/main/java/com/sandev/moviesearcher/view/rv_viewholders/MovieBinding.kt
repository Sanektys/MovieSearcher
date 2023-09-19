package com.sandev.moviesearcher.view.rv_viewholders

import com.example.domain_api.local_database.entities.DatabaseMovie


interface MovieBinding {
    fun onBind(databaseMovieData: DatabaseMovie)
}