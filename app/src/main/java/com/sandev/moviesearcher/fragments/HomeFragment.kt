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
import androidx.core.view.postDelayed
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.mockData


class HomeFragment : MoviesListFragment() {

    private var moviesRecyclerManager: RecyclerView.LayoutManager? = null
    private var moviesRecyclerAdapter: MoviesRecyclerAdapter? = null
    override var lastSearch: CharSequence? = null

    private val mainActivity by lazy { activity as MainActivity }

    companion object {
        private var isFragmentClassOnceCreated = false
        
        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = layoutInflater.inflate(R.layout.fragment_home, container, false)

        setAllTransitionAnimation(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeMovieRecyclerList(view)
        moviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            MOVIES_RECYCLER_VIEW_STATE))

        setupSearchBehavior(moviesRecyclerAdapter, mockData)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager?.onSaveInstanceState())
    }

    private fun initializeMovieRecyclerList(view: View) {
        if (moviesRecyclerAdapter == null) {
            moviesRecyclerAdapter = MoviesRecyclerAdapter()
            moviesRecyclerAdapter?.setList(mockData)
        }
        moviesRecyclerAdapter?.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ImageView) {
                resetTransitionAnimation(true)
                mainActivity.startDetailsFragment(movie, posterView)
            }
        })

        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true
        recyclerView.adapter = moviesRecyclerAdapter

        moviesRecyclerManager = recyclerView.layoutManager!!

        recyclerView.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setAllTransitionAnimation(rootView: View) {
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val scene = Scene.getSceneForLayout(rootView as ViewGroup, R.layout.merge_fragment_home_content, requireContext())
        if (!isFragmentClassOnceCreated) {  // запускать анимацию появления только при первой загрузке класса фрагмента
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
            isFragmentClassOnceCreated = true
        } else {
            scene.enter()
        }
        initializeViewsReferences(rootView)

        if (mainActivity.previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Не включать transition анимации после выхода из окна деталей
            rootView.postDelayed(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong()) {
                setTransitionAnimation(Gravity.START, true)
            }
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetTransitionAnimation(true)
        } else {
            setTransitionAnimation(Gravity.START, true)
        }
    }
}