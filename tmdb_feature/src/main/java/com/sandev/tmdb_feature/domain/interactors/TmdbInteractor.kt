package com.sandev.tmdb_feature.domain.interactors

import com.example.domain_api.local_database.repository.MoviesListRepository
import com.example.domain_api.the_movie_database.api.TmdbApi
import com.example.domain_impl.the_movie_database.constants.TmdbApiKey
import com.example.domain_api.the_movie_database.dto.TmdbMoviesListDto
import com.example.domain_impl.local_database.repositories.PlayingMoviesListRepository
import com.example.domain_impl.local_database.repositories.PopularMoviesListRepository
import com.example.domain_impl.local_database.repositories.TopMoviesListRepository
import com.example.domain_impl.local_database.repositories.UpcomingMoviesListRepository
import com.sandev.tmdb_feature.domain.converters.convertApiDtoListToMovieList
import com.sandev.tmdb_feature.domain.converters.convertApiDtoListToPlayingMovieList
import com.sandev.tmdb_feature.domain.converters.convertApiDtoListToPopularMovieList
import com.sandev.tmdb_feature.domain.converters.convertApiDtoListToTopMovieList
import com.sandev.tmdb_feature.domain.converters.convertApiDtoListToUpcomingMovieList
import com.example.domain_api.local_database.entities.DatabaseMovie
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.Locale


class TmdbInteractor(
    private val retrofitService: TmdbApi,
    private vararg val moviesListRepositories: MoviesListRepository
) {

    private val systemLanguage = Locale.getDefault().toLanguageTag()

    private var popularMoviesRepositoryIndex: Int = 0
    private var topMoviesRepositoryIndex: Int = 0
    private var upcomingMoviesRepositoryIndex: Int = 0
    private var playingMoviesRepositoryIndex: Int = 0

    init {
        for (i in moviesListRepositories.indices) {
            when (moviesListRepositories[i]) {
                is PopularMoviesListRepository -> popularMoviesRepositoryIndex = i
                is TopMoviesListRepository -> topMoviesRepositoryIndex = i
                is UpcomingMoviesListRepository -> upcomingMoviesRepositoryIndex = i
                is PlayingMoviesListRepository -> playingMoviesRepositoryIndex = i
            }
        }
    }


    fun getMoviesFromApi(page: Int, moviesCategory: String, repositoryType: RepositoryType)
            = retrofitService.getMovies(
        apiKey = TmdbApiKey.KEY,
        category = moviesCategory,
        language = systemLanguage,
        page = page
    ).subscribeOn(Schedulers.io()).map {
        val movies = when (repositoryType) {
            RepositoryType.TOP_MOVIES -> convertApiDtoListToTopMovieList(it.results)
            RepositoryType.POPULAR_MOVIES -> convertApiDtoListToPopularMovieList(it.results)
            RepositoryType.PLAYING_MOVIES -> convertApiDtoListToPlayingMovieList(it.results)
            RepositoryType.UPCOMING_MOVIES -> convertApiDtoListToUpcomingMovieList(it.results)
        }
        TmdbMoviesListDto(movies = movies, totalPages = it.totalPages)
    }.observeOn(Schedulers.io())

    fun getSearchedMoviesFromApi(query: String, page: Int)
            = retrofitService.getSearchedMovies(
        apiKey = TmdbApiKey.KEY,
        query = query,
        language = systemLanguage,
        page = page
    ).subscribeOn(Schedulers.io()).map {
        val movies = convertApiDtoListToMovieList(it.results)
        TmdbMoviesListDto(movies = movies, totalPages = it.totalPages)
    }.observeOn(Schedulers.io())

    fun putMoviesToDB(moviesList: List<DatabaseMovie>, repositoryType: RepositoryType)
            = Completable.create { emitter ->
        getRequestedRepository(repositoryType).putToDB(moviesList)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun deleteAllMoviesFromDbAndPutNewMovies(moviesList: List<DatabaseMovie>, repositoryType: RepositoryType)
            = Completable.create { emitter ->
        getRequestedRepository(repositoryType).deleteAllFromDBAndPutNew(moviesList)
        emitter.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getMoviesFromDB(page: Int, moviesPerPage: Int, repositoryType: RepositoryType)
            = Single.create<List<DatabaseMovie>> { emitter ->
        emitter.onSuccess(
            getRequestedRepository(repositoryType).getFromDB(
                from = (page - 1) * moviesPerPage,
                moviesCount = moviesPerPage
            )
        )
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    fun getSearchedMoviesFromDB(query: String, page: Int, moviesPerPage: Int, repositoryType: RepositoryType)
            = Single.create<List<DatabaseMovie>> { emitter ->
        emitter.onSuccess(
            getRequestedRepository(repositoryType).getSearchedFromDB(
                query = query,
                from = (page - 1) * moviesPerPage,
                moviesCount = moviesPerPage
            )
        )
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    private fun getRequestedRepository(repositoryType: RepositoryType): MoviesListRepository {
        return when (repositoryType) {
            RepositoryType.POPULAR_MOVIES -> moviesListRepositories[popularMoviesRepositoryIndex]
            RepositoryType.TOP_MOVIES -> moviesListRepositories[topMoviesRepositoryIndex]
            RepositoryType.UPCOMING_MOVIES -> moviesListRepositories[upcomingMoviesRepositoryIndex]
            RepositoryType.PLAYING_MOVIES -> moviesListRepositories[playingMoviesRepositoryIndex]
        }
    }


    enum class RepositoryType {
        POPULAR_MOVIES,
        TOP_MOVIES,
        UPCOMING_MOVIES,
        PLAYING_MOVIES
    }


    companion object {
        const val INITIAL_MOVIES_COUNT_PER_PAGE = 20
    }
}