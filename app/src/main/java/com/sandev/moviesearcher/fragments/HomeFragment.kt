package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.graphics.Outline
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.*
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.sandev.moviesearcher.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.movieListRecyclerView.adapter.MoviesRecyclerAdapter
import com.sandev.moviesearcher.movieListRecyclerView.data.Movie
import com.sandev.moviesearcher.movieListRecyclerView.data.setMockData


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)
        postponeEnterTransition()  // не запускать анимацию возвращения постера в список пока не просчитается recycler

        setAppBarAppearance(rootView)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeToolbar(view)
        initializeMovieRecyclerList(view)
    }

    private fun initializeToolbar(view: View) {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        val appToolbar: MaterialToolbar = view.findViewById(R.id.app_toolbar)
        appToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(requireContext(), R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun initializeMovieRecyclerList(view: View) {
        val moviesRecyclerAdapter = MoviesRecyclerAdapter(object : MoviesRecyclerAdapter.OnClickListener {
            override fun onClick(movie: Movie, posterView: ImageView)
                    = (activity as MainActivity).startDetailsFragment(movie, posterView)
        })
        moviesRecyclerAdapter.setList(setMockData())

        val moviesListRecycler: RecyclerView = view.findViewById(R.id.movies_list_recycler)
        moviesListRecycler.adapter = moviesRecyclerAdapter
        MainActivity.moviesRecyclerManager = moviesListRecycler.layoutManager!!

        moviesListRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.posters_appearance)
        moviesListRecycler.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setAppBarAppearance(rootView: View) {
        rootView.findViewById<AppBarLayout>(R.id.app_bar).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                insets
            }
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, -MainActivity.APP_BARS_CORNER_RADIUS.toInt(),
                        view!!.width, view.height,
                        MainActivity.APP_BARS_CORNER_RADIUS
                    )
                }
            }
            clipToOutline = true
        }
    }
}