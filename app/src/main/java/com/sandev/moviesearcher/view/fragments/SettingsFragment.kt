package com.sandev.moviesearcher.view.fragments

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.graphics.Outline
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.CheckedTextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.databinding.FragmentSettingsBinding
import com.sandev.moviesearcher.domain.WatchMovieNotification
import com.sandev.moviesearcher.utils.changeAppearanceToSamsungOneUI
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

        initializeCategoryButton()
        initializeAppThemeButton()
        initializeAppLanguageButton()
        initializeMovieNotificationButton()
        initializeSplashScreenSwitch()
        initializeRatingDonutSwitch()
    }

    override fun onResume() {
        super.onResume()

        if (!isSettingsScreenRevealed) {
            (parentFragment as MoviesListFragment).revealSettingsFragment()
            isSettingsScreenRevealed = true
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
    }

    override fun onPause() {
        super.onPause()

        onBackPressed.remove()
    }

    private fun destroy() = (parentFragment as MoviesListFragment).destroySettingsFragment()

    private fun initializeCategoryButton() {
        val roundButtons = arrayOf(
            getString(R.string.settings_fragment_radio_group_category_popular),
            getString(R.string.settings_fragment_radio_group_category_top_rated),
            getString(R.string.settings_fragment_radio_group_category_upcoming),
            getString(R.string.settings_fragment_radio_group_category_playing)
        )

        binding.categoryButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_fragment_alert_dialog_category_title)
                .setSingleChoiceItems(
                    roundButtons,
                    when (viewModel.getCategoryProperty.value) {
                        SharedPreferencesProvider.CATEGORY_POPULAR  -> RADIO_BUTTON_CATEGORY_POPULAR
                        SharedPreferencesProvider.CATEGORY_TOP      -> RADIO_BUTTON_CATEGORY_TOP
                        SharedPreferencesProvider.CATEGORY_UPCOMING -> RADIO_BUTTON_CATEGORY_UPCOMING
                        SharedPreferencesProvider.CATEGORY_PLAYING  -> RADIO_BUTTON_CATEGORY_PLAYING
                        else -> return@setOnClickListener
                    }
                ) { dialogInterface, which ->
                    when (which) {
                        RADIO_BUTTON_CATEGORY_POPULAR -> viewModel.putCategoryProperty(
                            SharedPreferencesProvider.CATEGORY_POPULAR
                        )
                        RADIO_BUTTON_CATEGORY_TOP -> viewModel.putCategoryProperty(
                            SharedPreferencesProvider.CATEGORY_TOP
                        )
                        RADIO_BUTTON_CATEGORY_UPCOMING -> viewModel.putCategoryProperty(
                            SharedPreferencesProvider.CATEGORY_UPCOMING
                        )
                        RADIO_BUTTON_CATEGORY_PLAYING -> viewModel.putCategoryProperty(
                            SharedPreferencesProvider.CATEGORY_PLAYING
                        )
                    }
                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.settings_fragment_alert_dialog_action_cancel, null)
                .create()
                .changeAppearanceToSamsungOneUI(Gravity.CENTER)
                .show()
        }

        viewModel.getCategoryProperty.observe(viewLifecycleOwner) { newlySetCategory ->
            when (newlySetCategory) {
                SharedPreferencesProvider.CATEGORY_POPULAR  -> binding.categoryButton.description.text =
                    getString(R.string.settings_fragment_movie_category_description,
                        getString(R.string.settings_fragment_radio_group_category_popular))

                SharedPreferencesProvider.CATEGORY_TOP      -> binding.categoryButton.description.text =
                    getString(R.string.settings_fragment_movie_category_description,
                        getString(R.string.settings_fragment_radio_group_category_top_rated))

                SharedPreferencesProvider.CATEGORY_UPCOMING -> binding.categoryButton.description.text =
                    getString(R.string.settings_fragment_movie_category_description,
                        getString(R.string.settings_fragment_radio_group_category_upcoming))

                SharedPreferencesProvider.CATEGORY_PLAYING  -> binding.categoryButton.description.text =
                    getString(R.string.settings_fragment_movie_category_description,
                        getString(R.string.settings_fragment_radio_group_category_playing))
            }
        }
    }

    private fun initializeAppThemeButton() {
        val radioButtons = arrayOf(
            getString(R.string.settings_fragment_radio_group_night_mode_off),
            getString(R.string.settings_fragment_radio_group_night_mode_on),
            getString(R.string.settings_fragment_radio_group_night_mode_default)
        )

        binding.appThemeButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_fragment_alert_dialog_night_mode_title)
                .setSingleChoiceItems(
                    radioButtons,
                    when (viewModel.getAppTheme.value) {
                        SharedPreferencesProvider.NIGHT_MODE_OFF     -> RADIO_BUTTON_NIGHT_MODE_OFF
                        SharedPreferencesProvider.NIGHT_MODE_ON      -> RADIO_BUTTON_NIGHT_MODE_ON
                        SharedPreferencesProvider.NIGHT_MODE_DEFAULT -> RADIO_BUTTON_NIGHT_MODE_DEFAULT
                        else -> return@setOnClickListener
                    }
                ) { dialogInterface, which ->
                    when (which) {
                        RADIO_BUTTON_NIGHT_MODE_OFF -> viewModel.setAppTheme(
                            SharedPreferencesProvider.NIGHT_MODE_OFF
                        )

                        RADIO_BUTTON_NIGHT_MODE_ON -> viewModel.setAppTheme(
                            SharedPreferencesProvider.NIGHT_MODE_ON
                        )

                        RADIO_BUTTON_NIGHT_MODE_DEFAULT -> viewModel.setAppTheme(
                            SharedPreferencesProvider.NIGHT_MODE_DEFAULT
                        )
                    }
                    dialogInterface.dismiss()
                }
                .setNegativeButton(R.string.settings_fragment_alert_dialog_action_cancel, null)
                .create()
                .changeAppearanceToSamsungOneUI(Gravity.CENTER)
                .show()
        }

        viewModel.getAppTheme.observe(viewLifecycleOwner) { newlySetTheme ->
            when (newlySetTheme) {
                SharedPreferencesProvider.NIGHT_MODE_OFF     -> binding.appThemeButton.description.text =
                    getString(R.string.settings_fragment_radio_group_night_mode_off)

                SharedPreferencesProvider.NIGHT_MODE_ON      -> binding.appThemeButton.description.text =
                    getString(R.string.settings_fragment_radio_group_night_mode_on)

                SharedPreferencesProvider.NIGHT_MODE_DEFAULT -> binding.appThemeButton.description.text =
                    getString(R.string.settings_fragment_radio_group_night_mode_default)
            }
        }
    }

    private fun initializeAppLanguageButton() {
        binding.appLanguageButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_fragment_alert_dialog_language_title)
                .setMessage(R.string.settings_fragment_alert_dialog_language_description)
                .setView(R.layout.alert_dialog_content_for_app_language)
                .setNegativeButton(R.string.settings_fragment_alert_dialog_action_cancel) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .create()
                .changeAppearanceToSamsungOneUI(Gravity.CENTER)
                .apply {
                    val russianLanguageRadioButton = findViewById<CheckedTextView>(R.id.radioButtonLanguageRussian)
                    val englishLanguageRadioButton = findViewById<CheckedTextView>(R.id.radioButtonLanguageEnglish)

                    viewModel.getAppLanguage.observe(this) { newlySetLanguage ->
                        when (newlySetLanguage) {
                            SharedPreferencesProvider.LANGUAGE_RUSSIAN -> {
                                russianLanguageRadioButton?.isChecked = true
                                englishLanguageRadioButton?.isChecked = false
                                dismiss()
                            }

                            SharedPreferencesProvider.LANGUAGE_ENGLISH -> {
                                englishLanguageRadioButton?.isChecked = true
                                russianLanguageRadioButton?.isChecked = false
                                dismiss()
                            }
                        }
                    }

                    russianLanguageRadioButton?.setOnClickListener {
                        viewModel.setAppLanguage(SharedPreferencesProvider.LANGUAGE_RUSSIAN)
                    }
                    englishLanguageRadioButton?.setOnClickListener {
                        viewModel.setAppLanguage(SharedPreferencesProvider.LANGUAGE_ENGLISH)
                    }
                }
                .show()
        }

        viewModel.getAppLanguage.observe(viewLifecycleOwner) { newlySetLanguage ->
            when (newlySetLanguage) {
                SharedPreferencesProvider.LANGUAGE_RUSSIAN -> binding.appLanguageButton.description.text =
                    getString(R.string.settings_fragment_radio_group_language_russian)

                SharedPreferencesProvider.LANGUAGE_ENGLISH -> binding.appLanguageButton.description.text =
                    getString(R.string.settings_fragment_radio_group_language_english)
            }
        }
    }

    private fun initializeMovieNotificationButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.movieNotificationButton.setOnClickListener {
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, WatchMovieNotification.CHANNEL_ID)
                }
                startActivity(intent)
            }
        } else {
            val linearLayout = binding.movieNotificationButton.parent as ViewGroup
            val indexOfDivider = linearLayout.indexOfChild(binding.movieNotificationButton) + 1
            linearLayout.removeViewAt(indexOfDivider)
            linearLayout.removeView(binding.movieNotificationButton)
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
        private const val SWITCH_VIBRATION_LENGTH = 50L

        private const val RADIO_BUTTON_CATEGORY_POPULAR  = 0
        private const val RADIO_BUTTON_CATEGORY_TOP      = 1
        private const val RADIO_BUTTON_CATEGORY_UPCOMING = 2
        private const val RADIO_BUTTON_CATEGORY_PLAYING  = 3

        private const val RADIO_BUTTON_NIGHT_MODE_OFF = 0
        private const val RADIO_BUTTON_NIGHT_MODE_ON  = 1
        private const val RADIO_BUTTON_NIGHT_MODE_DEFAULT = 2
    }
}