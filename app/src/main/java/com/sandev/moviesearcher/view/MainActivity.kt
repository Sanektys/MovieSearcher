package com.sandev.moviesearcher.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Outline
import android.os.BatteryManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.imageview.ShapeableImageView
import com.sandev.moviesearcher.BuildConfig
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.data.SharedPreferencesProvider
import com.sandev.moviesearcher.databinding.ActivityMainBinding
import com.sandev.moviesearcher.utils.broadcast_receivers.BatteryBroadcastReceiver
import com.sandev.moviesearcher.view.fragments.DetailsFragment
import com.sandev.moviesearcher.view.fragments.FavoritesFragment
import com.sandev.moviesearcher.view.fragments.HomeFragment
import com.sandev.moviesearcher.view.fragments.MoviesListFragment
import com.sandev.moviesearcher.view.fragments.PromotionFragment
import com.sandev.moviesearcher.view.fragments.SettingsFragment
import com.sandev.moviesearcher.view.fragments.SplashScreenFragment
import com.sandev.moviesearcher.view.fragments.WatchLaterFragment
import com.sandev.moviesearcher.view.viewmodels.MainActivityViewModel
import com.sandev.tmdb_feature.TmdbComponentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass


class MainActivity : AppCompatActivity() {

    private val viewModel: MainActivityViewModel by lazy {
        val tmdbComponent = ViewModelProvider(this)[TmdbComponentViewModel::class.java]

        val viewModelFactory = MainActivityViewModel.ViewModelFactory(tmdbComponent.interactor)
        ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]
    }

    var previousFragment: KClass<*>? = null

    private var backPressedLastTime: Long = 0

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private var homeFragment: HomeFragment? = HomeFragment()
    private var favoritesFragment: FavoritesFragment? = FavoritesFragment()
    private var watchLaterFragment: WatchLaterFragment? = WatchLaterFragment()

    private var batteryBroadcastReceiver = BatteryBroadcastReceiver(
        onBatteryLow = this::onBatteryLevelLow,
        onBatteryOkay = this::onBatteryLevelOkay
    )

    private val dummyOnBackPressed = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {}
    }

    private var sharedPreferencesCallback: SharedPreferences.OnSharedPreferenceChangeListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (viewModel.isPrimaryInitializationPerformed.not()) {
            checkCurrentAppTheme()
            checkBatteryLevel()
            viewModel.isPrimaryInitializationPerformed = true
        }

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkCurrentLocaleInSystemSettings()

        if (savedInstanceState == null) {
            checkForPromotedMovie()
        }

        setSystemBarsAppearanceAndBehavior()
        setNavigationBarAppearance(savedInstanceState)
        setOnBackPressedAction()
        menuButtonsInitial()

        registerSharedPreferencesChangeListener()
        registerBroadcastReceiver()

        initializeFragmentsCallbacks()

        if (supportFragmentManager.backStackEntryCount == 0) {
            startSplashScreen()
        } else {
            showGreetingsScreen(isAnimated = false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        removeSharedPreferencesChangeListener()
        unregisterReceiver(batteryBroadcastReceiver)

        homeFragment = null
        favoritesFragment = null
        watchLaterFragment = null

        _binding = null
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntentForLaunchSeparateDetailsFromNotification(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.navigationBarTranslationY = binding.navigationBar.translationY
        viewModel.navigationBarVisibility = binding.navigationBar.visibility
    }

    fun startHomeFragment() {
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragment, homeFragment!!)
            .addToBackStack(HOME_FRAGMENT_COMMIT)
            .commit()

        showGreetingsScreen(isAnimated = true)

        checkIntentForLaunchSeparateDetailsFromNotification(intent)
    }

    fun removeSplashScreen(splashScreenFragment: SplashScreenFragment) {
        supportFragmentManager
            .beginTransaction()
            .remove(splashScreenFragment)
            .commit()
    }

    private fun startSplashScreen() {
        val isSplashScreenEnabled = viewModel.isSplashScreenEnabled()

        if (!SplashScreenFragment.isSplashWasCreated && isSplashScreenEnabled) {
            if (supportFragmentManager.fragments.find { it is SplashScreenFragment } == null) {
                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.fragment, SplashScreenFragment())
                    .commit()
            }
        } else {
            startHomeFragment()
        }
    }

    private fun showGreetingsScreen(isAnimated: Boolean) {
        fun observeForPromotedMovie() = viewModel.getPromotedMovie.observe(this) { promotedMovie ->
            if (promotedMovie != null) {
                startPromotionFragment(promotedMovie)
                viewModel.getPromotedMovie.removeObservers(this)
                viewModel.isPromotedMovieShowed = true
            }
        }

        val isDemoScreenShowed = showDemoInfoScreenIfNeeded(isAnimated) {
            viewModel.sharedPreferencesInteractor.setShowingDemoInfoScreen(false)
            observeForPromotedMovie()
        }

        if (isDemoScreenShowed.not()) {
            observeForPromotedMovie()
        }
    }

    private fun showDemoInfoScreenIfNeeded(isAnimated: Boolean, onOkClick: () -> Unit): Boolean {
        if (BuildConfig.DEMO) {
            if (checkDemoExpired().not()) {
                if (viewModel.sharedPreferencesInteractor.isDemoInfoScreenShowing()) {
                    showDemoInfoScreen(
                        view = binding.root,
                        isAnimated = isAnimated,
                        okButtonCallback = onOkClick
                    )
                    return true
                }
            } else {
                Toast.makeText(this, getString(R.string.home_fragment_toast_demo_expired), Toast.LENGTH_LONG).show()
            }
        }
        return false
    }

    private fun checkIntentForLaunchSeparateDetailsFromNotification(intent: Intent?) = intent?.run {
        val movieFromNotification = getParcelableExtra(MOVIE_DATA_KEY) as DatabaseMovie?
        if (movieFromNotification != null) {
            startDetailsFragment(movieFromNotification)
        }
    }


    private fun registerBroadcastReceiver() {
        val filters = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        registerReceiver(batteryBroadcastReceiver, filters)
    }

    private fun checkBatteryLevel() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        if (batteryLevel == 0 || batteryLevel == Integer.MIN_VALUE) {
            // Альтернативный вариант получения уровня заряда, если код выше не сработал. НО работает "асинхронно", splashscreen уже отработает к моменту получения
            val batteryLevelReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    unregisterReceiver(this)

                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                    if (level > 0 && scale > 0) {
                        if (level * BATTERY_MAX_LEVEL / scale <= BATTERY_LOW_LEVEL) {
                            onBatteryLevelLow()
                        } else {
                            onBatteryLevelOkay()
                        }
                    }
                }
            }

            IntentFilter(Intent.ACTION_BATTERY_CHANGED).also {
                registerReceiver(batteryLevelReceiver, it)
            }
        } else {
            if (batteryLevel <= BATTERY_LOW_LEVEL) {
                onBatteryLevelLow()
            } else {
                onBatteryLevelOkay()
            }
        }
    }

    private fun menuButtonsInitial() {
        binding.navigationBar.apply {
            setOnItemSelectedListener { menuItem ->
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                if (lastFragmentInBackStack is MoviesListFragment) {
                    if (lastFragmentInBackStack.isSearchViewHidden()) {
                        navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                    } else {
                        lastFragmentInBackStack.hideSearchView()
                        lifecycleScope.launch {
                            while (true) {
                                if (lastFragmentInBackStack.isSearchViewHidden()) break
                                delay(LOOP_CYCLE_DELAY)
                            }
                            navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                        }
                        false
                    }
                } else {
                    navigationMenuItemClick(lastFragmentInBackStack, menuItem)
                }
            }
            // Navigation bar будет отслеживать backstack чтобы вовремя переключать кнопки меню
            supportFragmentManager.addOnBackStackChangedListener {
                if (supportFragmentManager.fragments.isNotEmpty()) {
                    when (supportFragmentManager.fragments.last()) {
                        is HomeFragment -> {
                            menu.findItem(R.id.bottom_navigation_all_movies_button).isChecked = true
                        }
                        is WatchLaterFragment -> {
                            menu.findItem(R.id.bottom_navigation_watch_later_button).isChecked = true
                        }
                        is FavoritesFragment -> {
                            menu.findItem(R.id.bottom_navigation_favorites_button).isChecked = true
                        }
                    }
                } else {
                    menu.forEach { it.isChecked = false }
                }
            }
        }
    }

    private fun navigationMenuItemClick(lastFragmentInBackStack: Fragment, menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.bottom_navigation_all_movies_button -> {
                if (lastFragmentInBackStack !is HomeFragment) {
                    supportFragmentManager.popBackStackWithSavingFragments(HOME_FRAGMENT_COMMIT)
                }
                true
            }
            R.id.bottom_navigation_watch_later_button -> {
                if (BuildConfig.DEMO) {
                    if (checkDemoExpiredWithToast(R.string.home_fragment_navigation_button_watch_later_toast_demo_expired)) return false
                }

                startFragmentFromNavigation(watchLaterFragment!!, WATCH_LATER_FRAGMENT_COMMIT)
                true
            }
            R.id.bottom_navigation_favorites_button -> {
                if (BuildConfig.DEMO) {
                    if (checkDemoExpiredWithToast(R.string.home_fragment_navigation_button_favorite_toast_demo_expired)) return false
                }

                startFragmentFromNavigation(favoritesFragment!!, FAVORITES_FRAGMENT_COMMIT)
                true
            }
            else -> false
        }
    }

    private fun startFragmentFromNavigation(fragment: Fragment, commitName: String) {
        val lastFragmentInBackStack = supportFragmentManager.fragments.last()
        if (lastFragmentInBackStack != fragment) {
            previousFragment = lastFragmentInBackStack::class
            var fragmentAlreadyExists = false
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                if (supportFragmentManager.getBackStackEntryAt(i).name == commitName) {
                    fragmentAlreadyExists = true
                }
            }
            if (!fragmentAlreadyExists) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(commitName)
                    .commit()
            } else {
                supportFragmentManager.popBackStackWithSavingFragments(commitName)
            }
        }
    }

    private fun initiateDetailsFragment(movie: DatabaseMovie, sharedView: ShapeableImageView): DetailsFragment {
        val bundle = Bundle().apply {
            putParcelable(MOVIE_DATA_KEY, movie)
            putString(POSTER_TRANSITION_KEY, sharedView.transitionName)
        }
        val detailsFragment = DetailsFragment().apply {
            arguments = bundle
        }
        return detailsFragment
    }

    fun startDetailsFragment(databaseMovie: DatabaseMovie, posterView: ShapeableImageView) {
        previousFragment = supportFragmentManager.fragments.last()::class
        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .addSharedElement(posterView, posterView.transitionName)
            .replace(R.id.fragment, initiateDetailsFragment(databaseMovie, posterView))
            .addToBackStack(null)
            .commit()
    }

    private fun startDetailsFragment(databaseMovie: DatabaseMovie) {
        val bundle = Bundle().also {
            it.putParcelable(MOVIE_DATA_KEY, databaseMovie)
            it.putBoolean(DetailsFragment.KEY_SEPARATE_DETAILS_FRAGMENT, true)
        }
        val detailsFragment = DetailsFragment().apply {
            arguments = bundle
        }

        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setSystemBarsAppearanceAndBehavior() {
        // Отменяем коллизию status & navigation bars, чтобы наши вьюхи проходили под ними
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Для альбомной ориентации убираем системные кнопки навигации с возможностью вытащить их по жесту
            WindowInsetsControllerCompat(window, binding.root).apply {
                hide(WindowInsetsCompat.Type.navigationBars())
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }

    private fun FragmentManager.popBackStackWithSavingFragments(commitName: String = "") {
        fun saveAndPop() {
            val lastFragmentInBackStack = fragments.last()
            when (lastFragmentInBackStack) {
                is HomeFragment -> homeFragment       = lastFragmentInBackStack
                is WatchLaterFragment -> watchLaterFragment = lastFragmentInBackStack
                is FavoritesFragment -> favoritesFragment  = lastFragmentInBackStack
            }
            previousFragment = lastFragmentInBackStack::class
            popBackStack()
        }

        if (commitName.isNotEmpty()) {
            for (i in (0 until backStackEntryCount).reversed()) {
                if (getBackStackEntryAt(i).name == commitName) return
                saveAndPop()
            }
        } else {
            saveAndPop()
        }
    }

    private fun setOnBackPressedAction() {
        onBackPressedDispatcher.addCallback(this, dummyOnBackPressed)
        onBackPressedDispatcher.addCallback(this,  object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val lastFragmentInBackStack = supportFragmentManager.fragments.last()
                if (lastFragmentInBackStack is MoviesListFragment) {
                    if (!lastFragmentInBackStack.isSearchViewHidden()) {
                        // Т.к. searchView не убирается сразу, то нужно ждать пока оно закроется
                        lastFragmentInBackStack.hideSearchView()
                        lifecycleScope.launch {
                            while (true) {
                                if (lastFragmentInBackStack.isSearchViewHidden()) break
                                delay(LOOP_CYCLE_DELAY)
                            }
                            isEnabled = true
                            dummyOnBackPressed.isEnabled = false
                            runOnUiThread {
                                onBackPressedDispatcher.onBackPressed()
                            }
                        }
                        // На время закрытия searchView не обрабатывать клики
                        dummyOnBackPressed.isEnabled = true
                        isEnabled = false
                        return
                    }
                }
                val backPressedTime = System.currentTimeMillis()
                if (supportFragmentManager.backStackEntryCount <= ONE_FRAGMENT_IN_STACK) {
                    if (backPressedLastTime + BACK_DOUBLE_TAP_THRESHOLD >= backPressedTime) {
                        finish()
                    } else {
                        Toast.makeText(this@MainActivity,
                            R.string.activity_main_press_back_for_exit_warning, Toast.LENGTH_SHORT).show()
                    }
                    backPressedLastTime = backPressedTime
                } else if (lastFragmentInBackStack is DetailsFragment) {
                    if (lastFragmentInBackStack.collapsingToolbarExpanded()) {
                        val promotionFragment = supportFragmentManager.fragments.find { it is PromotionFragment }
                        if (promotionFragment != null) {  //  Удаление с одновременным pop чтобы экран промоушена не восстановился через backstack
                            supportFragmentManager.beginTransaction().remove(promotionFragment).commitNow()
                            supportFragmentManager.popBackStackImmediate(SHOW_PROMOTED_MOVIE_COMMIT, POP_BACK_STACK_INCLUSIVE)
                        } else {
                            supportFragmentManager.popBackStackWithSavingFragments()
                        }
                    }
                } else {
                    supportFragmentManager.popBackStackWithSavingFragments()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == DetailsFragment.EXTERNAL_WRITE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                supportFragmentManager.fragments.forEach { fragment ->
                    if (fragment is DetailsFragment) {
                        fragment.performAsyncLoadOfPoster()
                        return
                    }
                }
            }
        }
    }

    private fun setNavigationBarAppearance(savedInstanceState: Bundle?) {
        binding.navigationBar.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
                updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
                updateLayoutParams {
                    height = resources.getDimensionPixelSize(R.dimen.activity_main_bottom_navigation_bar_height) + paddingBottom
                }
                insets
            }

            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(
                        0, 0,
                        view!!.width, (view.height + resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large)),
                        resources.getDimensionPixelSize(R.dimen.general_corner_radius_extra_large).toFloat()
                    )
                    outline?.alpha = 0f
                }
            }
            clipToOutline = true

            if (savedInstanceState != null) {
                translationY = viewModel.navigationBarTranslationY
                visibility = viewModel.navigationBarVisibility
            }
        }
    }

    private fun onBatteryLevelLow() {
        viewModel.sharedPreferencesInteractor.setSplashScreenSwitchButtonState(false)
        viewModel.sharedPreferencesInteractor.setRatingDonutSwitchButtonState(false)
        Toast.makeText(this, getString(R.string.activity_main_toast_message_low_battery), Toast.LENGTH_LONG).show()
    }

    private fun onBatteryLevelOkay() {
        viewModel.sharedPreferencesInteractor.setSplashScreenSwitchButtonState(true)
        viewModel.sharedPreferencesInteractor.setRatingDonutSwitchButtonState(true)
    }

    private fun checkCurrentAppTheme() {
        when (viewModel.sharedPreferencesInteractor.getAppTheme()) {
            SharedPreferencesProvider.NIGHT_MODE_OFF ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

            SharedPreferencesProvider.NIGHT_MODE_ON ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

            SharedPreferencesProvider.NIGHT_MODE_DEFAULT ->
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun checkCurrentLocaleInSystemSettings() {
        val currentLocale = AppCompatDelegate.getApplicationLocales()[0]
        when (currentLocale?.language) {
            Locale.forLanguageTag(SharedPreferencesProvider.LANGUAGE_RUSSIAN).language ->
                viewModel.sharedPreferencesInteractor.setAppLanguage(SharedPreferencesProvider.LANGUAGE_RUSSIAN)

            Locale.forLanguageTag(SharedPreferencesProvider.LANGUAGE_ENGLISH).language ->
                viewModel.sharedPreferencesInteractor.setAppLanguage(SharedPreferencesProvider.LANGUAGE_ENGLISH)

            null -> {
                when (LocaleListCompat.getDefault()[0]!!.language) {
                    Locale.forLanguageTag(SharedPreferencesProvider.LANGUAGE_RUSSIAN).language ->
                        viewModel.sharedPreferencesInteractor.setAppLanguage(SharedPreferencesProvider.LANGUAGE_RUSSIAN)

                    Locale.forLanguageTag(SharedPreferencesProvider.LANGUAGE_ENGLISH).language ->
                        viewModel.sharedPreferencesInteractor.setAppLanguage(SharedPreferencesProvider.LANGUAGE_ENGLISH)
                }
            }
        }
        checkCurrentLocaleInAppSettings()
    }

    private fun checkCurrentLocaleInAppSettings() {
        when (viewModel.sharedPreferencesInteractor.getAppLanguage()) {
            SharedPreferencesProvider.LANGUAGE_RUSSIAN -> AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(SharedPreferencesProvider.LANGUAGE_RUSSIAN))

            SharedPreferencesProvider.LANGUAGE_ENGLISH -> AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(SharedPreferencesProvider.LANGUAGE_ENGLISH)
            )
        }
    }

    private fun registerSharedPreferencesChangeListener() {
        sharedPreferencesCallback = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                SharedPreferencesProvider.KEY_NIGHT_MODE -> checkCurrentAppTheme()
                SharedPreferencesProvider.KEY_LANGUAGE -> checkCurrentLocaleInAppSettings()
            }
        }
        viewModel.sharedPreferencesInteractor.addSharedPreferencesChangeListener(sharedPreferencesCallback!!)
    }

    private fun removeSharedPreferencesChangeListener() {
        viewModel.sharedPreferencesInteractor.removeSharedPreferencesChangeListener(
            sharedPreferencesCallback ?: return
        )
    }

    private fun initializeFragmentsCallbacks() {
        supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                fragment: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                when (fragment) {
                    is SplashScreenFragment -> {
                        binding.navigationBar.run {
                            doOnNextLayout { translationY = height.toFloat() }
                            menu.forEach { it.isEnabled = false }
                        }
                    }
                    is HomeFragment -> {
                        if (viewModel.isSplashScreenEnabled() || HomeFragment.isFragmentClassOnceCreated) {
                            binding.navigationBar.run {
                                animate()
                                    .translationY(0f)
                                    .setDuration(
                                        if (HomeFragment.isFragmentClassOnceCreated) {
                                            resources.getInteger(
                                                R.integer.activity_main_animations_durations_poster_transition
                                            ).toLong()
                                        } else {
                                            resources.getInteger(
                                                R.integer.activity_main_animations_durations_first_appearance_navigation_bar
                                            ).toLong()
                                        }
                                    )
                                    .setInterpolator(DecelerateInterpolator())
                                    .withStartAction {
                                        visibility = View.VISIBLE
                                        menu.forEach { it.isEnabled = true }
                                    }
                                    .start()
                            }
                        }
                    }
                    is MoviesListFragment -> {
                        binding.navigationBar.run {
                            animate()
                                .translationY(0f)
                                .setDuration(
                                    resources.getInteger(
                                        R.integer.activity_main_animations_durations_poster_transition
                                    ).toLong()
                                )
                                .setInterpolator(DecelerateInterpolator())
                                .withStartAction {
                                    visibility = View.VISIBLE
                                    menu.forEach { it.isEnabled = true }
                                }
                                .start()
                        }
                    }
                    is DetailsFragment -> {
                        binding.navigationBar.run {
                            doOnPreDraw {
                                animate()  // Убрать нижний navigation view
                                    .translationY(height.toFloat())
                                    .setDuration(
                                        resources.getInteger(R.integer.activity_main_animations_durations_poster_transition)
                                            .toLong()
                                    )
                                    .setInterpolator(AccelerateInterpolator())
                                    .withStartAction { menu.forEach { it.isEnabled = false } }
                                    .withEndAction { visibility = View.GONE }
                                    .start()
                            }
                        }
                    }
                    is SettingsFragment -> {
                        binding.navigationBar.run {
                            doOnPreDraw {
                                menu.forEach { item -> item.isEnabled = false }
                            }
                        }
                    }
                }
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, fragment: Fragment) {
                when (fragment) {
                    is SettingsFragment -> {
                        binding.navigationBar.run {
                            menu.forEach { item -> item.isEnabled = true }
                        }
                    }
                }
            }
        }, true)
    }

    private fun checkForPromotedMovie() {
        CoroutineScope(EmptyCoroutineContext).run {
            launch {
                viewModel.checkForCurrentMoviePromotion()
            }.invokeOnCompletion {
                cancel()
            }
        }
    }

    private fun startPromotionFragment(promotionMovie: DatabaseMovie) {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is PromotionFragment) return  // Фрагмент уже был добавлен
        }

        val bundle = Bundle().apply {
           putParcelable(MOVIE_DATA_KEY, promotionMovie)
        }
        val promotionFragment = PromotionFragment().apply {
            arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.promotion_fragment_appearance,
                R.anim.promotion_fragment_disappearance,
                R.anim.promotion_fragment_appearance,
                R.anim.promotion_fragment_disappearance
            )
            .add(R.id.fullscreenFragment, promotionFragment)
            .addToBackStack(SHOW_PROMOTED_MOVIE_COMMIT)
            .commitAllowingStateLoss()
    }

    fun finishPromotionFragment() {
        supportFragmentManager.popBackStack(SHOW_PROMOTED_MOVIE_COMMIT, POP_BACK_STACK_INCLUSIVE)
    }

    fun startDetailsFragmentFromPromotionFragment(
        removingFragment: PromotionFragment,
        databaseMovie: DatabaseMovie,
        posterView: ShapeableImageView
    ) {
        val transaction = supportFragmentManager
            .beginTransaction()

        supportFragmentManager.findFragmentById(R.id.fragment)?.run { // Текущий открытый список с фильмами
            previousFragment = this::class
            transaction.remove(this)
        }

        transaction
            .setReorderingAllowed(true)
            .addSharedElement(posterView, posterView.transitionName)
            .hide(removingFragment)  // Сначала прячем, а затем удаляем в другой транзакции чтобы фрагмент не вернулся с бэкстека
            .add(R.id.fullscreenFragment, initiateDetailsFragment(databaseMovie, posterView))
            .addToBackStack(null)
            .commit()
    }


    companion object {
        const val MOVIE_DATA_KEY = "MOVIE"
        const val POSTER_TRANSITION_KEY = "POSTER_TRANSITION"

        private const val HOME_FRAGMENT_COMMIT = "HOME_FRAGMENT_COMMIT"
        private const val FAVORITES_FRAGMENT_COMMIT = "FAVORITES_FRAGMENT_COMMIT"
        private const val WATCH_LATER_FRAGMENT_COMMIT = "WATCH_LATER_FRAGMENT_COMMIT"
        private const val SHOW_PROMOTED_MOVIE_COMMIT = "PROMOTED_MOVIE_COMMIT"

        private const val BACK_DOUBLE_TAP_THRESHOLD = 1500L
        private const val ONE_FRAGMENT_IN_STACK = 1
        private const val LOOP_CYCLE_DELAY = 50L
        private const val BATTERY_LOW_LEVEL = 15
        private const val BATTERY_MAX_LEVEL = 100
    }
}