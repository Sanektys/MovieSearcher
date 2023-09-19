package com.sandev.cached_movies_feature.watch_later_movies.domain

import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.example.domain_api.local_database.repository.WatchLaterListRepository
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers


class WatchLaterMoviesInteractor(override val repo: WatchLaterListRepository) : CachedMoviesInteractor(repo) {

    fun getAllWatchLaterMoviesFromList(): Observable<List<WatchLaterDatabaseMovie>>
            = repo.getAllWatchMoviesLaterFromDB()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getFewWatchLaterMoviesFromList(from: Int, moviesCount: Int): Single<List<WatchLaterDatabaseMovie>>
            = Single.create<List<WatchLaterDatabaseMovie>> { emitter ->
        emitter.onSuccess(repo.getWatchLaterMoviesFromDB(from = from, moviesCount = moviesCount))
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getFewSearchedWatchLaterMoviesFromList(query: String, from: Int, moviesCount: Int): Single<List<WatchLaterDatabaseMovie>>
            = Single.create<List<WatchLaterDatabaseMovie>> { emitter ->
        emitter.onSuccess(repo.getSearchedWatchLaterMovieFromDB(query = query, from = from, moviesCount = moviesCount))
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun updateWatchLaterNotificationDate(movie: WatchLaterDatabaseMovie) = Completable.create { emitter ->
        repo.updateNotificationDate(movie)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}