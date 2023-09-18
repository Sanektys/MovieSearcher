package com.sandev.moviesearcher.view.viewmodels

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.example.domain_api.local_database.entities.WatchLaterDatabaseMovie
import com.sandev.cached_movies_feature.domain.CachedMoviesInteractor
import com.sandev.cached_movies_feature.watch_later_movies.domain.WatchLaterMoviesInteractor
import com.sandev.moviesearcher.utils.workers.WorkRequests
import com.sandev.moviesearcher.view.dialogs.DateTimePickerDialog
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.view.rv_adapters.WatchLaterRecyclerAdapter
import com.sandev.moviesearcher.view.rv_viewholders.WatchLaterMovieViewHolder
import io.reactivex.rxjava3.disposables.Disposable


class WatchLaterFragmentViewModel(override val cachedMoviesInteractor: WatchLaterMoviesInteractor)
    : SavedMoviesListViewModel(cachedMoviesInteractor) {

    override val recyclerAdapter: MoviesRecyclerAdapter
            = WatchLaterRecyclerAdapter(sharedPreferencesInteractor.isRatingDonutAnimationEnabled())

    override var lastSearch: String
        set(value) { Companion.lastSearch = value }
        get() = Companion.lastSearch
    override var isInSearchMode: Boolean
        set(value) { Companion.isInSearchMode = value }
        get() = Companion.isInSearchMode

    var isLaunchedFromLeft: Boolean = true


    init {
        sharedPreferencesInteractor.addSharedPreferencesChangeListener(recyclerAdapter.sharedPreferencesCallback)

        registerMovieInListStateChangeObservers()

        dispatchQueryToInteractor(page = INITIAL_PAGE_IN_RECYCLER)
    }


    fun blockOnClickCallbacksOnMovieCardElementsAndMovieDeletion() {
        super.blockCallbackOnPosterClickAndMovieDeletion()
        (recyclerAdapter as WatchLaterRecyclerAdapter).setOnScheduleNotificationButtonClick(null)
    }

    fun unblockOnClickCallbacksOnMovieCardElements(onPosterClick: MoviesRecyclerAdapter.OnPosterClickListener,
                                                   onScheduleButtonClick: WatchLaterRecyclerAdapter.ScheduleNotificationButtonClick) {
        super.unblockCallbackOnPosterClick(onPosterClick)
        (recyclerAdapter as WatchLaterRecyclerAdapter).setOnScheduleNotificationButtonClick(onScheduleButtonClick)
    }

    fun rescheduleMovieNotificationDate(
        activity: FragmentActivity,
        viewHolder: WatchLaterMovieViewHolder, movie: WatchLaterDatabaseMovie,
        @StringRes datePickerTitle: Int?, @StringRes timePickerTitle: Int?
    ) {
        DateTimePickerDialog.show(
            activity = activity, datePickerTitle = datePickerTitle, timePickerTitle = timePickerTitle
        ) { newDate ->
            WorkRequests.enqueueWatchLaterNotificationWork(activity, movie, newDate)

            movie.notificationDate = newDate
            viewHolder.setTextOfNotificationScheduledDate(movie)
        }
    }

    fun removeMovieFromWatchLaterListAndSchedule(context: Context, movie: DatabaseMovie) {
        WorkRequests.cancelWatchLaterNotificationWork(context, movie)
        removeFromSavedList(movie)
    }

    override fun getMoviesFromDB(offset: Int) {
        var disposable: Disposable? = null
        disposable = cachedMoviesInteractor.getFewWatchLaterMoviesFromList(
            from = offset,
            moviesCount = CachedMoviesInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }

    override fun getSearchedMoviesFromDB(query: String, offset: Int) {
        var disposable: Disposable? = null
        disposable = cachedMoviesInteractor.getFewSearchedWatchLaterMoviesFromList(
            query = query,
            from = offset,
            moviesCount = CachedMoviesInteractor.MOVIES_PER_PAGE
        ).subscribe { movies ->
            moviesList.value = movies
            disposable?.dispose()
        }
    }


    class ViewModelFactory(private val cachedMoviesInteractor: WatchLaterMoviesInteractor) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {

            if (modelClass.isAssignableFrom(WatchLaterFragmentViewModel::class.java)) {
                return WatchLaterFragmentViewModel(cachedMoviesInteractor) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    companion object {
        private var lastSearch: String = ""
        private var isInSearchMode: Boolean = false
    }
}
