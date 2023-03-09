package com.sandev.moviesearcher.fragments

import android.animation.AnimatorInflater
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Display
import android.view.View
import android.view.ViewOutlineProvider
import android.view.WindowMetrics
import android.widget.Toast
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.search.SearchBar
import com.sandev.moviesearcher.R


abstract class MoviesListFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setAppBarAppearance(view)
        setRecyclerViewAppearance(view)
        initializeToolbar(view)
    }

    private fun initializeToolbar(view: View) {
        val settingsButton: View = view.findViewById(R.id.top_toolbar_settings_button)
        settingsButton.stateListAnimator = AnimatorInflater.loadStateListAnimator(requireContext(), R.animator.settings_button_spin)

        val searchBar: SearchBar = view.findViewById(R.id.search_bar)
        searchBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.top_toolbar_settings_button -> {
                    Toast.makeText(requireContext(), R.string.activity_main_top_app_bar_settings_title, Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setAppBarAppearance(rootView: View) {
        rootView.findViewById<AppBarLayout>(R.id.app_bar).apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
                insets
            }
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    val originHeight = height - paddingTop.toFloat()
                    val deltaHeight = (originHeight + view!!.top) / originHeight
                    outline?.setRoundRect(
                        0, -resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large),
                        view.width, view.height,
                        resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large).toFloat() *
                                deltaHeight  // Чем больше раскрыт app bar, тем больше скругление углов
                    )
                }
            }
            clipToOutline = true
            // Задаём цвет переднего плана для дальнейшего перекрытия им app bar при сворачивании
            foreground = ColorDrawable(context.getColor(R.color.md_theme_secondaryContainer))
            foreground.alpha = 0

            val recycler = rootView.findViewById<RecyclerView>(R.id.movies_list_recycler)
            doOnLayout {
                // Слушатель смещения app bar закрашивающий search bar и обновляющий размеры recycler
                addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
                    val expandedOffset = height - paddingTop.toFloat()

                    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                        foreground.alpha = (LinearOutSlowInInterpolator()
                            .getInterpolation(-verticalOffset / expandedOffset) * 255f).toInt()

                        recycler.invalidateOutline()
                    }
                })
            }
        }
    }

    private fun setRecyclerViewAppearance(rootView: View) {
        rootView.findViewById<RecyclerView>(R.id.movies_list_recycler).apply {
            val appBar: AppBarLayout = rootView.findViewById(R.id.app_bar)
            doOnPreDraw {
                outlineProvider = object : ViewOutlineProvider() {
                    val bottomNavigation =
                        requireActivity().findViewById<BottomNavigationView>(R.id.navigation_bar)
                    val visibleDisplayFrame = Rect()
                    val holdingPlaces = appBar.height - appBar.paddingTop +
                            bottomNavigation.height - bottomNavigation.paddingBottom +
                            resources.getDimensionPixelSize(R.dimen.activity_main_movies_recycler_margin_vertical) * 2
                    val freeSpace: Int

                    init {
                        getWindowVisibleDisplayFrame(visibleDisplayFrame)
                        freeSpace = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            visibleDisplayFrame.height() - holdingPlaces
                        } else {
                            height
                        }
                    }

                    override fun getOutline(view: View?, outline: Outline?) {
                        // Прямо в методе закругления краёв обновляем высоту recycler, всё равно этот метод
                        // вызывается при каждом изменении размеров вьюхи
                        view!!.updateLayoutParams {
                            height = freeSpace - appBar.top
                        }
                        outline?.setRoundRect(
                            0, 0, view.width, view.height,
                            resources.getDimensionPixelSize(R.dimen.general_corner_radius_large)
                                .toFloat()
                        )
                    }
                }
                clipToOutline = true
            }
        }
    }
}