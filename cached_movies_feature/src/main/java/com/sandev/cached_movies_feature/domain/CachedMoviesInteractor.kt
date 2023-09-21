package com.sandev.cached_movies_feature.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.repository.MoviesListRepositoryForSavedLists
import com.example.domain_impl.local_database.repositories.MoviesListRepositoryForSavedListsImpl
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers


open class CachedMoviesInteractor(protected open val repo: MoviesListRepositoryForSavedLists) {

    private val deletedDatabaseMovie = MutableLiveData<DatabaseMovie>()
    val getDeletedDatabaseMovie: LiveData<DatabaseMovie> = deletedDatabaseMovie

    private val movieAddedNotify = MutableLiveData<Nothing>()
    val getMovieAddedNotify: LiveData<Nothing> = movieAddedNotify


    fun addToList(databaseMovie: DatabaseMovie) = Completable.create { emitter ->
        repo.putToDB(listOf(databaseMovie))
        movieAddedNotify.postValue(null)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun removeFromList(databaseMovie: DatabaseMovie) = Completable.create { emitter ->
        (repo as MoviesListRepositoryForSavedListsImpl).deleteFromDB(databaseMovie)
        deletedDatabaseMovie.postValue(databaseMovie)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getAllFromList(): Observable<List<DatabaseMovie>> = repo.getAllFromDB()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getFewMoviesFromList(from: Int, moviesCount: Int): Single<List<DatabaseMovie>>
            = Single.create<List<DatabaseMovie>> { emitter ->
        emitter.onSuccess(repo.getFromDB(from = from, moviesCount = moviesCount))
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getFewSearchedMoviesFromList(query: String, from: Int, moviesCount: Int): Single<List<DatabaseMovie>>
            = Single.create<List<DatabaseMovie>> { emitter ->
        emitter.onSuccess(repo.getSearchedFromDB(query = query, from = from, moviesCount = moviesCount))
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


    companion object {
        const val MOVIES_PER_PAGE = 15
    }
}