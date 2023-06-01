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
import androidx.core.view.forEach
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentSettingsBinding


class SettingsFragment : Fragment() {

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

        view.doOnAttach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressed)
        }
    }

    private fun destroy() = (parentFragment as MoviesListFragment).destroySettingsFragment()

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