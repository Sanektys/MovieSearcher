package com.example.domain_api.the_movie_database.dto

import com.example.domain_api.dto.Movie
import java.util.Objects


data class MovieDto(
    override val id: Int = 0,
    override val poster: String?,
    override val title: String,
    override val description: String,
    override var rating: Float = 0f
): Movie {

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is MovieDto) return false

        if (other === this) return true

        // В проверке не участвует ID
        return Objects.equals(this.poster, other.poster) &&
                Objects.equals(this.title, other.title) &&
                Objects.equals(this.description, other.description) &&
                Objects.equals(this.rating, other.rating)
    }

    override fun hashCode(): Int {
        return Objects.hash(poster, title, description, rating)
    }
}
