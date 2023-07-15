package com.sandev.moviesearcher.domain.interactors

import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.data.db.entities.Movie
import com.sandev.moviesearcher.data.repositories.MoviesListRepository
import com.sandev.moviesearcher.data.repositories.PlayingMoviesListRepository
import com.sandev.moviesearcher.data.repositories.PopularMoviesListRepository
import com.sandev.moviesearcher.data.repositories.TopMoviesListRepository
import com.sandev.moviesearcher.data.repositories.UpcomingMoviesListRepository
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApi
import com.sandev.moviesearcher.data.themoviedatabase.TmdbApiKey
import com.sandev.moviesearcher.data.themoviedatabase.TmdbResult
import com.sandev.moviesearcher.utils.TmdbConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Singleton


@Singleton
class TmdbInteractor(private val retrofitService: TmdbApi,
                     private val sharedPreferences: SharedPreferencesProvider,
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


    suspend fun getMoviesFromApi(page: Int, repositoryType: RepositoryType): Flow<TmdbResult> = flow {
        emit(retrofitService.getMovies(
            apiKey = TmdbApiKey.KEY,
            category = sharedPreferences.getDefaultCategory(),
            language = systemLanguage,
            page = page
        ))
    }.map {
        val movies = when (repositoryType) {
            RepositoryType.TOP_MOVIES      -> TmdbConverter.convertApiDtoListToTopMovieList(it.results)
            RepositoryType.POPULAR_MOVIES  -> TmdbConverter.convertApiDtoListToPopularMovieList(it.results)
            RepositoryType.PLAYING_MOVIES  -> TmdbConverter.convertApiDtoListToPlayingMovieList(it.results)
            RepositoryType.UPCOMING_MOVIES -> TmdbConverter.convertApiDtoListToUpcomingMovieList(it.results)
        }
        TmdbResult(movies = movies, totalPages = it.totalPages)
    }.flowOn(Dispatchers.IO)

    suspend fun getSearchedMoviesFromApi(query: String, page: Int): Flow<TmdbResult> = flow {
        emit(retrofitService.getSearchedMovies(
            apiKey = TmdbApiKey.KEY,
            query = query,
            language = systemLanguage,
            page = page
        ))
    }.map {
        val movies = TmdbConverter.convertApiDtoListToMovieList(it.results)
        TmdbResult(movies = movies, totalPages = it.totalPages)
    }.flowOn(Dispatchers.IO)

    suspend fun putMoviesToDB(moviesList: List<Movie>, repositoryType: RepositoryType)
            = withContext(Dispatchers.IO) {
        getRequestedRepository(repositoryType).putToDB(moviesList)
    }

    suspend fun deleteAllMoviesFromDbAndPutNewMovies(moviesList: List<Movie>, repositoryType: RepositoryType)
            = withContext(Dispatchers.IO) {
        getRequestedRepository(repositoryType).deleteAllFromDBAndPutNew(moviesList)
    }

    suspend fun getMoviesFromDB(page: Int, moviesPerPage: Int, repositoryType: RepositoryType)
            = withContext(Dispatchers.IO) {
        getRequestedRepository(repositoryType).getFromDB(
            from = (page - 1) * moviesPerPage,
            moviesCount = moviesPerPage
        )
    }

    suspend fun getSearchedMoviesFromDB(query: String, page: Int, moviesPerPage: Int, repositoryType: RepositoryType)
            = withContext(Dispatchers.IO) {
        getRequestedRepository(repositoryType).getSearchedFromDB(
            query = query,
            from = (page - 1) * moviesPerPage,
            moviesCount = moviesPerPage
        )
    }

    private fun getRequestedRepository(repositoryType: RepositoryType): MoviesListRepository {
        return when (repositoryType) {
            RepositoryType.POPULAR_MOVIES  -> moviesListRepositories[popularMoviesRepositoryIndex]
            RepositoryType.TOP_MOVIES      -> moviesListRepositories[topMoviesRepositoryIndex]
            RepositoryType.UPCOMING_MOVIES -> moviesListRepositories[upcomingMoviesRepositoryIndex]
            RepositoryType.PLAYING_MOVIES  -> moviesListRepositories[playingMoviesRepositoryIndex]
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