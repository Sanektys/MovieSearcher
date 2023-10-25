package com.sandev.moviesearcher.view.fragments

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.domain.constants.TmdbCommonConstants
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentPromotionBinding
import com.sandev.moviesearcher.view.MainActivity


class PromotionFragment : Fragment(R.layout.fragment_promotion) {

    private var _binding: FragmentPromotionBinding? = null
    private val binding: FragmentPromotionBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentPromotionBinding.bind(view)

        val promotionMovie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(MainActivity.MOVIE_DATA_KEY, DatabaseMovie::class.java)
        } else {
            arguments?.getParcelable<DatabaseMovie>(MainActivity.MOVIE_DATA_KEY)
        }
        if (promotionMovie == null) {
            requireActivity().supportFragmentManager.popBackStack()
            return
        }

        initializePromotionScreenAppearance(promotionMovie)
        initializePromotionButtons(promotionMovie)

        animatePosterClickHintDisappearance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializePromotionScreenAppearance(promotionMovie: DatabaseMovie) {
        binding.promotionMessage.apply {
            val paddingVertical = resources.getDimensionPixelSize(R.dimen.activity_main_movie_promotion_headline_verticalPadding)

            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                setContentPadding(
                    0,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top + paddingVertical,
                    0,
                    paddingVertical
                )
                insets
            }
        }
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.promotionDecisionButtons.apply {
                ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                    translationY = -(insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom / 2f)
                    insets
                }
            }
        }

        binding.promotionMovieTitle.text = promotionMovie.title
        Glide.with(this)
            .load("${TmdbCommonConstants.IMAGES_URL}${TmdbCommonConstants.IMAGE_MEDIUM_SIZE}${promotionMovie.poster}")
            .placeholder(R.drawable.dummy_poster)
            .apply(RequestOptions().dontTransform())
            .into(binding.promotionMoviePoster)
    }

    private fun animatePosterClickHintDisappearance() {
        binding.promotionMoviePosterHint?.apply {
            alpha = 1f

            animate()
                .setDuration(resources.getInteger(R.integer.activity_main_movie_promotion_animation_appearance_duration).toLong())
                .setStartDelay(resources.getInteger(R.integer.activity_main_movie_promotion_poster_hint_animation_delay).toLong())
                .alpha(0f)
        }
    }

    private fun initializePromotionButtons(promotionMovie: DatabaseMovie) {
        fun clearButtonsListeners() {
            _binding?.okButton?.setOnClickListener {}
            _binding?.cancelButton?.setOnClickListener {}
            _binding?.posterHolder?.setOnClickListener {}
        }
        fun openMovieDetails() {
            clearButtonsListeners()
            (activity as? MainActivity)?.startDetailsFragmentFromPromotionFragment(this, promotionMovie, binding.promotionMoviePoster)
        }

        binding.cancelButton.setOnClickListener {
            clearButtonsListeners()
            (activity as? MainActivity)?.finishPromotionFragment()
        }
        binding.okButton.setOnClickListener {
            openMovieDetails()
        }
        binding.posterHolder.setOnClickListener {
            openMovieDetails()
        }
    }
}