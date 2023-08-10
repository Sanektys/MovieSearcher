package com.example.domain_impl.local_database.repositories

import com.example.domain_api.local_database.daos.MovieDao


class PopularMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class TopMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class UpcomingMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)

class PlayingMoviesListRepository(movieDao: MovieDao) : MoviesListRepositoryImpl(movieDao)