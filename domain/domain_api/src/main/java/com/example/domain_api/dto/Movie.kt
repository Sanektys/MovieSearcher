package com.example.domain_api.dto


interface Movie {

    val id: Int
    val poster: String?
    val title: String
    val description: String
    var rating: Float
}