package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
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
                (activity as MainActivity).startDetailsFragment(movie, posterView)
            }
        })

        val moviesListRecycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        moviesListRecycler.setHasFixedSize(true)
        moviesListRecycler.isNestedScrollingEnabled = true
        moviesListRecycler.adapter = moviesRecyclerAdapter
        moviesRecyclerManager = moviesListRecycler.layoutManager!!

        moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
        moviesListRecycler.doOnPreDraw { startPostponedEnterTransition() }
    }
}