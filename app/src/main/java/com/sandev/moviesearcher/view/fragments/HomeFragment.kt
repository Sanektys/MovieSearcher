package com.sandev.moviesearcher.view.fragments

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.*
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentHomeBinding
import com.sandev.moviesearcher.databinding.MergeFragmentHomeContentBinding
import com.sandev.moviesearcher.view.rv_adapters.MoviesRecyclerAdapter
import com.sandev.moviesearcher.domain.Movie
import com.sandev.moviesearcher.view.viewmodels.HomeFragmentViewModel
import com.sandev.moviesearcher.view.viewmodels.MoviesListFragmentViewModel


class HomeFragment : MoviesListFragment() {

    override val viewModel: MoviesListFragmentViewModel by lazy {
        ViewModelProvider.NewInstanceFactory().create(HomeFragmentViewModel::class.java)
    }

    private var _bindingFull: MergeFragmentHomeContentBinding? = null
    private val bindingFull: MergeFragmentHomeContentBinding
        get() = _bindingFull!!
    private var _bindingBlank: FragmentHomeBinding? = null
    private val bindingBlank: FragmentHomeBinding
        get() = _bindingBlank!!

    private var mainActivity: MainActivity? = null
    private var moviesRecyclerManager: RecyclerView.LayoutManager? = null
    override var recyclerAdapter: MoviesRecyclerAdapter?
        set(value) {
            Companion.recyclerAdapter = value
        }
        get() = Companion.recyclerAdapter

    companion object {
        var isFragmentClassOnceCreated = false
            private set

        private const val MOVIES_RECYCLER_VIEW_STATE = "MoviesRecylerViewState"

        private var recyclerAdapter: MoviesRecyclerAdapter? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _bindingBlank = FragmentHomeBinding.inflate(inflater, container, false)

        mainActivity = activity as MainActivity
        setAllTransitionAnimation()

        return bindingBlank.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.moviesListLiveData.observe(viewLifecycleOwner) { database ->
            moviesDatabase = database
        }

        initializeMovieRecyclerList()
        moviesRecyclerManager?.onRestoreInstanceState(savedInstanceState?.getParcelable(
            MOVIES_RECYCLER_VIEW_STATE
        ))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MOVIES_RECYCLER_VIEW_STATE, moviesRecyclerManager?.onSaveInstanceState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingFull = null
        _bindingBlank = null
        moviesRecyclerManager = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isChangingConfigurations == false) {
            recyclerAdapter = null
        }
    }

    private fun initializeMovieRecyclerList() {
        if (recyclerAdapter == null) {
            recyclerAdapter = MoviesRecyclerAdapter()
            initializeRecyclerAdapter()
        }
        recyclerAdapter?.setPosterOnClickListener(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ImageView) {
                resetExitReenterTransitionAnimations()
                mainActivity?.startDetailsFragment(movie, posterView)
            }
        })

        bindingFull.moviesListRecycler.apply {
            setHasFixedSize(true)
            isNestedScrollingEnabled = true
            adapter = recyclerAdapter

            moviesRecyclerManager = layoutManager!!

            doOnPreDraw { startPostponedEnterTransition() }
        }
    }

    private fun setAllTransitionAnimation() {
        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        val scene = Scene.getSceneForLayout(bindingBlank.root as ViewGroup, R.layout.merge_fragment_home_content, requireContext())
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
        initializeViewsReferences(bindingBlank.root)

        _bindingFull = MergeFragmentHomeContentBinding.bind(bindingBlank.root)

        if (mainActivity?.previousFragmentName == DetailsFragment::class.qualifiedName) {
            // Не включать transition анимации после выхода из окна деталей
            bindingFull.root.postDelayed(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong()) {
                setTransitionAnimation(Gravity.START)
            }
            // LayoutAnimation для recycler включается только при возвращении с экрана деталей
            bindingFull.moviesListRecycler.layoutAnimation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
            resetExitReenterTransitionAnimations()
        } else {
            setTransitionAnimation(Gravity.START)
        }
    }
}