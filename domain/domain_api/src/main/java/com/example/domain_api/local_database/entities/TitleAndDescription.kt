package com.example.domain_api.local_database.entities

import androidx.room.ColumnInfo


// Класс для поиска совпадений по базе на основании и названия и описания фильма
data class TitleAndDescription(
    @ColumnInfo(name = DatabaseMovie.COLUMN_TITLE)
    val title: String,
    @ColumnInfo(name = DatabaseMovie.COLUMN_DESCRIPTION)
    val description: String
)
