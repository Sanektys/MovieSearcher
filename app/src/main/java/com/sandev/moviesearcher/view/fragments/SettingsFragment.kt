package com.sandev.moviesearcher.view.fragments

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentSettingsBinding
import com.sandev.moviesearcher.view.viewmodels.SettingsFragmentViewModel


class SettingsFragment : Fragment() {

    private val viewModel: SettingsFragmentViewModel by lazy {
        val factory = SettingsFragmentViewModel.MyViewModelFactory(requireContext())
        ViewModelProvider(this, factory)[SettingsFragmentViewModel::class.java]
    }

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    private val onBackPressed = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = destroy()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance()
        setSearchBarAppearance()
        initializeCategoryRadioGroup()

        view.doOnAttach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
        }
    }

    private fun destroy() = (parentFragment as MoviesListFragment).destroySettingsFragment()

    private fun initializeCategoryRadioGroup() {
        viewModel.getCategoryProperty.observe(viewLifecycleOwner) { currentCategory ->
            when (currentCategory) {
                viewModel.categoryPopular -> binding.categoryRadioGroup.check(R.id.RadioButtonPopular)
                viewModel.categoryTopRated -> binding.categoryRadioGroup.check(R.id.RadioButtonTopRated)
                viewModel.categoryUpcoming -> binding.categoryRadioGroup.check(R.id.RadioButtonUpcoming)
                viewModel.categoryNowPlaying -> binding.categoryRadioGroup.check(R.id.RadioButtonNowPlaying)
            }
        }

        binding.categoryRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.RadioButtonPopular -> viewModel.putCategoryProperty(viewModel.categoryPopular)
                R.id.RadioButtonTopRated -> viewModel.putCategoryProperty(viewModel.categoryTopRated)
                R.id.RadioButtonUpcoming -> viewModel.putCategoryProperty(viewModel.categoryUpcoming)
                R.id.RadioButtonNowPlaying -> viewModel.putCategoryProperty(viewModel.categoryNowPlaying)
            }
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