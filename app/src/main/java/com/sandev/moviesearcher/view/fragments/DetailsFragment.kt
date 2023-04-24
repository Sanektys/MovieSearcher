package com.sandev.moviesearcher.view.fragments

import android.content.Intent
import android.graphics.Outline
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.DecelerateInterpolator
import androidx.core.os.bundleOf
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentDetailsBinding
import com.sandev.moviesearcher.data.Movie
import com.sandev.moviesearcher.data.favoriteMovies
import com.sandev.moviesearcher.data.watchLaterMovies


class DetailsFragment : Fragment() {

    private val movie by lazy(LazyThreadSafetyMode.NONE) { arguments?.getParcelable<Movie>(
        MainActivity.MOVIE_DATA_KEY)!! }

    private var isFavoriteMovie: Boolean = false
    private var isWatchLaterMovie: Boolean = false
    private var configurationChanged = false

    private var fragmentThatLaunchedDetails: String? = null

    private var _binding: FragmentDetailsBinding? = null
    private val binding: FragmentDetailsBinding
        get() = _binding!!

    companion object {
        private const val FRAGMENT_LAUNCHED_KEY = "FRAGMENT_LAUNCHED"
        private const val FAVORITE_BUTTON_SELECTED_KEY = "FAVORITE_BUTTON_SELECTED"
        private const val WATCH_LATER_BUTTON_SELECTED_KEY = "WATCH_LATER_BUTTON_SELECTED"

        private const val TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        setToolbarAppearance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentThatLaunchedDetails = savedInstanceState?.getString(FRAGMENT_LAUNCHED_KEY) ?:
                (activity as MainActivity).previousFragmentName

        initializeContent()
        setToolbarBackButton()
        setFloatButtonOnClick()
        if (savedInstanceState != null) {
            binding.fabToFavorite.isSelected = savedInstanceState.getBoolean(
                FAVORITE_BUTTON_SELECTED_KEY
            )
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)
            binding.fabToWatchLater.isSelected = savedInstanceState.getBoolean(
                WATCH_LATER_BUTTON_SELECTED_KEY
            )
            binding.fabToWatchLater.setImageResource(R.drawable.watch_later_icon_selector)
        }

        setTransitionAnimation()

        activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
            animate()  // Убрать нижний navigation view
                .translationY(height.toFloat())
                .setDuration(resources.getInteger(R.integer.activity_main_animations_durations_poster_transition).toLong())
                .withStartAction { menu.forEach { it.isEnabled = false } }
                .withEndAction { visibility = GONE }
                .start()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FRAGMENT_LAUNCHED_KEY, fragmentThatLaunchedDetails)
        outState.putBoolean(FAVORITE_BUTTON_SELECTED_KEY, binding.fabToFavorite.isSelected)
        outState.putBoolean(WATCH_LATER_BUTTON_SELECTED_KEY, binding.fabToWatchLater.isSelected)
    }

    override fun onStop() {
        super.onStop()
        configurationChanged = requireActivity().isChangingConfigurations
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!configurationChanged) {
            activity?.findViewById<BottomNavigationView>(R.id.navigation_bar)?.run {
                animate()
                    .translationY(0f)
                    .setDuration(
                        resources.getInteger(R.integer.activity_main_animations_durations_poster_transition)
                            .toLong()
                    )
                    .withStartAction { visibility = VISIBLE; menu.forEach { it.isEnabled = true } }
                    .start()
            }
            // Принятие решения о добавлении/удалении фильма в избранном
            changeFavoriteMoviesList()
            changeWatchLaterMoviesList()
        }
        _binding = null
    }

    private fun setFloatButtonOnClick() {
        if (favoriteMovies.find { it.title == movie.title } != null) {
            isFavoriteMovie = true
            binding.fabToFavorite.isSelected = true
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)
        }

        binding.fabToFavorite.setOnClickListener {
            binding.fabToFavorite.isSelected = !binding.fabToFavorite.isSelected
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)

            Snackbar.make(requireContext(), binding.root,
                if (binding.fabToFavorite.isSelected) getString(R.string.details_fragment_fab_add_favorite)
                else getString(R.string.details_fragment_fab_remove_favorite),
                Snackbar.LENGTH_SHORT).show()
        }

        if (watchLaterMovies.find { it.title == movie.title } != null) {
            isWatchLaterMovie = true
            binding.fabToWatchLater.isSelected = true
            binding.fabToWatchLater.setImageResource(R.drawable.watch_later_icon_selector)
        }

        binding.fabToWatchLater.setOnClickListener {
            binding.fabToWatchLater.isSelected = !binding.fabToWatchLater.isSelected
            binding.fabToWatchLater.setImageResource(R.drawable.watch_later_icon_selector)

            Snackbar.make(requireContext(), binding.root,
                if (binding.fabToWatchLater.isSelected) getString(R.string.details_fragment_fab_add_watch_later)
                else getString(R.string.details_fragment_fab_remove_watch_later),
                Snackbar.LENGTH_SHORT).show()
        }

        binding.fabShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.details_fragment_fab_share_sending_text,
                movie.title, movie.description))
            startActivity(Intent.createChooser(intent, getString(R.string.details_fragment_fab_share_to)))
        }
    }

    private fun initializeContent() {
        binding.collapsingToolbarImage.apply {
            Glide.with(this@DetailsFragment).load(movie.poster).centerCrop().into(this)
            transitionName = arguments?.getString(MainActivity.POSTER_TRANSITION_KEY)
        }
        binding.title.text = movie.title
        binding.description.text = movie.description
    }

    private fun setToolbarAppearance() {
        binding.root.let { view ->
            // Установка верхнего паддинга у тулбара для того, чтобы он не провалился под статус бар
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
                val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top  // Если под статус бар можно "залезть", то вернётся его высота
                val appBarHeight = resources.getDimensionPixelSize(R.dimen.activity_details_app_bar_height)
                val toolbarSize = resources.getDimensionPixelSize(R.dimen.activity_details_app_bar_toolbar_height)

                binding.collapsingToolbarToolbar.apply {
                    updatePadding(top = topInset)  // Обновляем паддинг только у свёрнутого тулбара, иначе изображение съедет
                    updateLayoutParams { height = toolbarSize + paddingTop }  // Закономерно увеличиваем высоту тулбара, чтобы его контент не скукожило
                }
                binding.appBar.apply {
                    updateLayoutParams { height = appBarHeight + topInset }

                    // Также делаем закругление краёв снизу для тулбара
                    outlineProvider = object : ViewOutlineProvider() {
                        override fun getOutline(toolbar: View?, outline: Outline?) {
                            outline?.setRoundRect(0, -resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large),
                                toolbar!!.width, toolbar.height,
                                resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large).toFloat())
                        }
                    }
                    clipToOutline = true
                }
                binding.collapsingToolbar.apply {
                    // Обновляем позицию триггера перехода на свёрнутый тулбар, где фон заменяется на цвет
                    doOnPreDraw {
                        scrimVisibleHeightTrigger =
                            binding.collapsingToolbarToolbar.height *
                                    TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER
                    }
                }
                insets
            }
        }
    }

    private fun setToolbarBackButton() {
        binding.collapsingToolbarToolbar.apply {
            setNavigationIcon(R.drawable.round_arrow_back)
            setNavigationOnClickListener {
                if (binding.appBar.isLifted) {
                    binding.appBar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                        override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                            if (verticalOffset == 0) {  // collapsing toolbar полностью развёрнут
                                activity?.onBackPressedDispatcher?.onBackPressed()
                                binding.appBar.removeOnOffsetChangedListener(this)
                            }
                        }
                    })
                    binding.appBar.setExpanded(true, true)
                } else {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }
        }
    }

    fun collapsingToolbarExpanded(): Boolean {
        if (binding.appBar.isLifted) {
            binding.appBar.setExpanded(true, true)
            return false
        }
        return true
    }

    private fun changeFavoriteMoviesList() {
        if (!isFavoriteMovie && binding.fabToFavorite.isSelected) {
            isFavoriteMovie = true
            favoriteMovies.add(movie)
        } else if (isFavoriteMovie && !binding.fabToFavorite.isSelected) {
            isFavoriteMovie = false
            favoriteMovies.remove(movie)
            if (fragmentThatLaunchedDetails == FavoritesFragment::class.qualifiedName) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    FavoritesFragment.FAVORITES_DETAILS_RESULT_KEY,
                    bundleOf(FavoritesFragment.MOVIE_NOW_NOT_FAVORITE_KEY to true)
                )
            }
        }
    }

    private fun changeWatchLaterMoviesList() {
        if (!isWatchLaterMovie && binding.fabToWatchLater.isSelected) {
            isWatchLaterMovie = true
            watchLaterMovies.add(movie)
        } else if (isWatchLaterMovie && !binding.fabToWatchLater.isSelected) {
            isWatchLaterMovie = false
            watchLaterMovies.remove(movie)
            if (fragmentThatLaunchedDetails == WatchLaterFragment::class.qualifiedName) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    WatchLaterFragment.WATCH_LATER_DETAILS_RESULT_KEY,
                    bundleOf(WatchLaterFragment.MOVIE_NOW_NOT_WATCH_LATER_KEY to true)
                )
            }
        }
    }

    private fun setTransitionAnimation() {
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.poster_transition)

        val duration = resources.getInteger(R.integer.activity_main_animations_durations_poster_transition)
            .toLong()

        enterTransition = TransitionSet().apply {
            val appBarTransition = Fade().apply {
                mode = Fade.MODE_IN
                interpolator = DecelerateInterpolator()
                addTarget(binding.appBar)
            }
            val movieInformationTransition = Fade().apply {
                mode = Fade.MODE_IN
                interpolator = FastOutLinearInInterpolator()
                addTarget(binding.title)
                addTarget(binding.description)
            }
            val fabTransition = Slide(Gravity.END).apply {
                mode = Slide.MODE_IN
                interpolator = LinearOutSlowInInterpolator()
                addTarget(binding.fabToFavorite)
                addTarget(binding.fabToWatchLater)
                addTarget(binding.fabShare)
            }
            this.duration = duration
            addTransition(appBarTransition)
            addTransition(movieInformationTransition)
            addTransition(fabTransition)
        }
    }
}