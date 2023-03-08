package com.sandev.moviesearcher.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData


class HomeFragment : MoviesListFragment() {

    private var moviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var moviesRecyclerAdapter: MoviesRecyclerAdapter? = null

    companion object {
        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = layoutInflater.inflate(R.layout.fragment_home, container, false)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeMovieRecyclerList(view)
        moviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            MOVIES_RECYCLER_VIEW_STATE))
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