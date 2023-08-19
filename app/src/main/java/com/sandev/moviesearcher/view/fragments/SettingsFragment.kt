package com.sandev.moviesearcher.view.fragments

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewOutlineProvider
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.databinding.FragmentSettingsBinding
import com.sandev.moviesearcher.view.viewmodels.SettingsFragmentViewModel


class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsFragmentViewModel by lazy {
        ViewModelProvider(this)[SettingsFragmentViewModel::class.java]
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val onBackPressed = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = destroy()
    }

    private var vibrator: Vibrator? = null
    private var isSettingsScreenRevealed = false
    private var isSplashScreenSwitchInitialized = false
    private var isRatingDonutSwitchInitialized = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSettingsBinding.bind(view)

        vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setAppBarAppearance()
        setSearchBarAppearance()
        setNestedScrollAppearance()

        initializeCategoryRadioGroup()
        initializeSplashScreenSwitch()
        initializeRatingDonutSwitch()

        view.doOnAttach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
        }
    }

    override fun onStart() {
        super.onStart()

        if (!isSettingsScreenRevealed) {
            (parentFragment as MoviesListFragment).revealSettingsFragment()
            isSettingsScreenRevealed = true
        }
    }

    private fun destroy() = (parentFragment as MoviesListFragment).destroySettingsFragment()

    private fun initializeCategoryRadioGroup() {
        viewModel.getCategoryProperty.observe(viewLifecycleOwner) { currentCategory ->
            when (currentCategory) {
                SharedPreferencesProvider.CATEGORY_TOP      -> binding.categoryRadioGroup.check(R.id.RadioButtonTopRated)
                SharedPreferencesProvider.CATEGORY_POPULAR  -> binding.categoryRadioGroup.check(R.id.RadioButtonPopular)
                SharedPreferencesProvider.CATEGORY_UPCOMING -> binding.categoryRadioGroup.check(R.id.RadioButtonUpcoming)
                SharedPreferencesProvider.CATEGORY_PLAYING  -> binding.categoryRadioGroup.check(R.id.RadioButtonNowPlaying)
            }
        }

        binding.categoryRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.RadioButtonTopRated   -> viewModel.putCategoryProperty(SharedPreferencesProvider.CATEGORY_TOP)
                R.id.RadioButtonPopular    -> viewModel.putCategoryProperty(SharedPreferencesProvider.CATEGORY_POPULAR)
                R.id.RadioButtonUpcoming   -> viewModel.putCategoryProperty(SharedPreferencesProvider.CATEGORY_UPCOMING)
                R.id.RadioButtonNowPlaying -> viewModel.putCategoryProperty(SharedPreferencesProvider.CATEGORY_PLAYING)
            }
        }
    }

    private fun initializeSplashScreenSwitch() {
        viewModel.getSplashScreenEnabling.observe(viewLifecycleOwner) { isSplashScreenEnabled ->
            if (isSplashScreenSwitchInitialized) {
                binding.splashScreenSwitch.switch.isChecked = isSplashScreenEnabled
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(SWITCH_VIBRATION_LENGTH, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator?.vibrate(SWITCH_VIBRATION_LENGTH)
                }
            } else {
                // Не показывать анимацию переключения и не включать вибрацию во время инициализации свитча
                binding.splashScreenSwitch.switch.isChecked = isSplashScreenEnabled
                binding.splashScreenSwitch.switch.jumpDrawablesToCurrentState()
                isSplashScreenSwitchInitialized = true
            }
        }

        binding.splashScreenSwitch.setOnClickListener {
            viewModel.setSplashScreenEnabling(!binding.splashScreenSwitch.switch.isChecked)
        }

        viewModel.getSplashScreenButtonState.observe(viewLifecycleOwner) { isButtonEnabled ->
            binding.splashScreenSwitch.isEnabled = isButtonEnabled
            binding.splashScreenSwitch.switch.isEnabled = isButtonEnabled
        }
    }

    private fun initializeRatingDonutSwitch() {
        viewModel.getRatingDonutAnimationState.observe(viewLifecycleOwner) { isRatingDonutAnimationEnabled ->
            if (isRatingDonutSwitchInitialized) {
                binding.ratingDonutSwitch.switch.isChecked = isRatingDonutAnimationEnabled
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(SWITCH_VIBRATION_LENGTH, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator?.vibrate(SWITCH_VIBRATION_LENGTH)
                }
            } else {
                binding.ratingDonutSwitch.switch.isChecked = isRatingDonutAnimationEnabled
                binding.ratingDonutSwitch.switch.jumpDrawablesToCurrentState()
                isRatingDonutSwitchInitialized = true
            }
        }

        binding.ratingDonutSwitch.setOnClickListener {
            viewModel.setRatingDonutAnimationState(!binding.ratingDonutSwitch.switch.isChecked)
        }

        viewModel.getRatingDonutButtonState.observe(viewLifecycleOwner) { isButtonEnabled ->
            binding.ratingDonutSwitch.isEnabled = isButtonEnabled
            binding.ratingDonutSwitch.switch.isEnabled = isButtonEnabled
        }
    }

    private fun setAppBarAppearance() {
        binding.appBarLayout.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { appBar, insets ->
                appBar.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                insets
            }
        }
    }

    private fun setSearchBarAppearance() {
        val settingsButton: View = binding.root.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        binding.searchBar.setNavigationOnClickListener {
            destroy()
        }
    }

    private fun setNestedScrollAppearance() {
        binding.settingsScroll.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, 0,
                        view!!.width, view.height,
                        resources.getDimensionPixelSize(R.dimen.fragment_settings_block_corner_radius)
                            .toFloat()
                    )
                    outline?.alpha = 0f
                }
            }
            clipToOutline = true
        }
    }


    companion object {
        const val SWITCH_VIBRATION_LENGTH = 50L
    }
}