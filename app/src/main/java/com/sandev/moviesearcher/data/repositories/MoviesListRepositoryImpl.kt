package com.sandev.moviesearcher.data.repositories

import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.db.dao.MovieDao
import com.sandev.moviesearcher.data.db.entities.Movie
import java.util.concurrent.Executors


open class MoviesListRepositoryImpl(protected val movieDao: MovieDao) : MoviesListRepository {

    protected val putToDbFlagLiveData = MutableLiveData<List<Long>>()
    protected val deletedRowsCountLiveData = MutableLiveData<Int>()


    override fun putToDB(movies: List<Movie>) {
        Executors.newSingleThreadExecutor().execute {
            putToDbFlagLiveData.postValue(movieDao.putToCachedMovies(movies))
        }
    }

    override fun getAllFromDB(): List<Movie> = movieDao.getAllCachedMovies()

    override fun getSearchedFromDB(query: String): List<Movie> = movieDao.getSearchedCachedMovies(query)

    override fun deleteAllFromDB() {
        Executors.newSingleThreadExecutor().execute {
            deletedRowsCountLiveData.postValue(movieDao.deleteAllCachedMovies())
        }
    }


    companion object {
        const val PUT_ERROR_FLAG = -1L
    }
}