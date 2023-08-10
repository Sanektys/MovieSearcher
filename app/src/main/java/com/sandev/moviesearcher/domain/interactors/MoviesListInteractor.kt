package com.sandev.moviesearcher.domain.interactors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sandev.moviesearcher.data.db.entities.DatabaseMovie
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.MoviesListRepositoryImplForSavedLists
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale
import javax.inject.Inject


class MoviesListInteractor @Inject constructor(private val repo: MoviesListRepository) : Interactor {

    override val systemLanguage = Locale.getDefault().toLanguageTag()

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
        (repo as MoviesListRepositoryImplForSavedLists).deleteFromDB(databaseMovie)
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