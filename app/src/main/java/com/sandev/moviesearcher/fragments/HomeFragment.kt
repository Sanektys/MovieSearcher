package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.google.android.material.search.SearchBar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData


class HomeFragment : MoviesListFragment() {

    private var moviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var moviesRecyclerAdapter: MoviesRecyclerAdapter? = null
    override var lastSearch: CharSequence? = null
    private var isFragmentClassCreated = false

    companion object {
        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = layoutInflater.inflate(R.layout.fragment_home, container, false)

        setTransitionAnimation(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeMovieRecyclerList(view)
        moviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            MOVIES_RECYCLER_VIEW_STATE))

        setupSearchBehavior(moviesRecyclerAdapter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList(view: View) {
        if (moviesRecyclerAdapter == null) {
            moviesRecyclerAdapter = MoviesRecyclerAdapter()
            moviesRecyclerAdapter!!.setList(setMockData())
        }
        moviesRecyclerAdapter!!.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ImageView) {
                resetDefaultTransitionAnimation()
                (activity as MainActivity).startDetailsFragment(movie, posterView)
            }
        })

        val moviesListRecycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        moviesListRecycler.setHasFixedSize(true)
        moviesListRecycler.isNestedScrollingEnabled = true
        moviesListRecycler.adapter = moviesRecyclerAdapter
        moviesRecyclerManager = moviesListRecycler.layoutManager!!

        moviesListRecycler.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setTransitionAnimation(rootView: View) {
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val scene = Scene.getSceneForLayout(rootView as ViewGroup, R.layout.merge_fragment_home_content, requireContext())
        if (!isFragmentClassCreated) {  // запускать анимацию первого появления только при создании класса
            val appBarSlideTransition = Slide(Gravity.TOP)
                .setDuration(resources.getInteger(
                    R.integer.activity_main_animations_durations_first_appearance_app_bar).toLong())
                .setInterpolator(DecelerateInterpolator())
                .addTarget(R.id.app_bar)
            val moviesRecyclerTransition = Slide(Gravity.BOTTOM)
                .setDuration(resources.getInteger(
                    R.integer.activity_main_animations_durations_first_appearance_recycler).toLong())
                .setInterpolator(DecelerateInterpolator())
                .addTarget(R.id.movies_list_recycler)
            val appearingTransition = TransitionSet().apply {
                addTransition(appBarSlideTransition)
                addTransition(moviesRecyclerTransition)
            }
            TransitionManager.go(scene, appearingTransition)
            isFragmentClassCreated = true
        } else {
            scene.enter()
        }

        val recycler: RecyclerView = rootView.findViewById(R.id.movies_list_recycler)
        if ((activity as MainActivity).previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Не включать transition анимации после выхода из окна деталей
            rootView.postDelayed(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong()) {
                setDefaultTransitionAnimation(rootView)
            }
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            recycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetDefaultTransitionAnimation()
        } else {
            setDefaultTransitionAnimation(rootView)
        }
    }

    override fun setDefaultTransitionAnimation(view: View) {
        val searchBar: SearchBar = view.findViewById(R.id.search_bar)
        val recycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        val duration = resources.getInteger(R.integer.general_animations_durations_fragment_transition).toLong()

        val recyclerTransition = Slide(Gravity.START).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addTarget(recycler)
        }
        val transitionSet = TransitionSet().addTransition(recyclerTransition)

        if (!isAppBarLifted) {
            val appBarTransition = Fade().apply {
                this.duration = duration
                interpolator = AccelerateInterpolator()
                addTarget(searchBar)
            }
            transitionSet.addTransition(appBarTransition)
        }
        exitTransition = transitionSet
        reenterTransition = transitionSet
    }

    private fun resetDefaultTransitionAnimation() {
        exitTransition = null
        reenterTransition = null
    }
}