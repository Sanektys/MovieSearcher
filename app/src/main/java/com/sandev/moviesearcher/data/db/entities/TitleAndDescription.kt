package com.sandev.moviesearcher.data.db.entities

import androidx.room.ColumnInfo


// Класс для поиска совпадений по базе на основании и названия и описания фильма
data class TitleAndDescription(
    @ColumnInfo(name = Movie.COLUMN_TITLE)
    val title: String,
    @ColumnInfo(name = Movie.COLUMN_DESCRIPTION)
    val description: String
)
