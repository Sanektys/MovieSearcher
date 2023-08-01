package com.sandev.moviesearcher.view.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.View
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentSettingsBinding.bind(view)

        setAppBarAppearance()
        setSearchBarAppearance()
        initializeCategoryRadioGroup()
        initializeSplashScreenSwitch()

        view.doOnAttach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
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
            binding.splashScreenSwitch.switch.isChecked = isSplashScreenEnabled
        }

        binding.splashScreenSwitch.setOnClickListener {
            viewModel.setSplashScreenEnabling(!binding.splashScreenSwitch.switch.isChecked)
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
}