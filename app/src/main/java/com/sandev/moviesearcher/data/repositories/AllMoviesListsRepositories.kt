package com.sandev.moviesearcher.data.repositories

import com.sandev.moviesearcher.data.db.dao.MovieDao


class PopularMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class TopMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class UpcomingMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class PlayingMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)