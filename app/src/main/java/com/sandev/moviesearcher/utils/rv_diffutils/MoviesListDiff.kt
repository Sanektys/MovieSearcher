package com.sandev.moviesearcher.utils.rv_diffutils

import androidx.recyclerview.widget.DiffUtil
import com.sandev.moviesearcher.data.db.entities.Movie


class MoviesListDiff(private val oldList: List<Movie>, private val newList: List<Movie>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].description == newList[newItemPosition].description
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].poster == newList[newItemPosition].poster
                && oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].description == newList[newItemPosition].description
    }
}