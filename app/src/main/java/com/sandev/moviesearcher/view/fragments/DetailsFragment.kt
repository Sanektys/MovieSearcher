package com.sandev.moviesearcher.view.fragments

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.domain.constants.TmdbCommonConstants
import com.example.domain_api.local_database.entities.DatabaseMovie
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sandev.cached_movies_feature.favorite_movies.FavoriteMoviesComponentViewModel
import com.sandev.cached_movies_feature.watch_later_movies.WatchLaterMoviesComponentViewModel
import com.sandev.moviesearcher.BuildConfig
import com.sandev.moviesearcher.R
import com.sandev.moviesearcher.databinding.FragmentDetailsBinding
import com.sandev.moviesearcher.utils.changeAppearanceToSamsungOneUI
import com.sandev.moviesearcher.utils.workers.WorkRequests
import com.sandev.moviesearcher.view.MainActivity
import com.sandev.moviesearcher.view.checkDemoExpiredWithToast
import com.sandev.moviesearcher.view.dialogs.DateTimePickerDialog
import com.sandev.moviesearcher.view.viewmodels.DetailsFragmentViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException


class DetailsFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this)[DetailsFragmentViewModel::class.java]
    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding: FragmentDetailsBinding
        get() = _binding!!

    private var menuFabDialog: AlertDialog? = null

    private var posterDownloadDisposable: Disposable? = null

    private val disposableContainer = CompositeDisposable()

    private val requestNotificationPermissionActivity = registerRequestNotificationPermissionActivity()


    override fun onAttach(context: Context) {
        super.onAttach(context)

        val favoritesMoviesDatabaseComponentFactory
                = FavoriteMoviesComponentViewModel.ViewModelFactory(context)
        val favoritesMoviesDatabaseComponent = ViewModelProvider(
            requireActivity(),
            favoritesMoviesDatabaseComponentFactory
        )[FavoriteMoviesComponentViewModel::class.java]
        viewModel.favoritesMoviesDatabaseInteractor = favoritesMoviesDatabaseComponent.interactor

        val watchLaterMoviesDatabaseComponentFactory
                = WatchLaterMoviesComponentViewModel.ViewModelFactory(context)
        val watchLaterMoviesDatabaseComponent = ViewModelProvider(
            requireActivity(),
            watchLaterMoviesDatabaseComponentFactory
        )[WatchLaterMoviesComponentViewModel::class.java]
        viewModel.watchLaterMoviesDatabaseInteractor = watchLaterMoviesDatabaseComponent.interactor
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)

        setToolbarAppearance()

        viewModel.isLowQualityPosterDownloaded = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (savedInstanceState == null) {
            viewModel._movie = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable(MainActivity.MOVIE_DATA_KEY, DatabaseMovie::class.java)
            } else {
                arguments?.getParcelable<DatabaseMovie>(MainActivity.MOVIE_DATA_KEY)!!
            }
            viewModel.isFragmentSeparate =
                arguments?.getBoolean(KEY_SEPARATE_DETAILS_FRAGMENT) ?: false

            if (viewModel.isFragmentSeparate.not()) {
                viewModel.fragmentThatLaunchedDetails = (activity as MainActivity).previousFragmentName
            }
        }

        initializeContent()
        setToolbarBackButton()
        setFloatButtonOnClick()

        if (savedInstanceState != null) {
            binding.fabToFavorite.isSelected = viewModel.isFavoriteButtonSelected
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)

            binding.fabToWatchLater.isSelected = viewModel.isWatchLaterButtonSelected
            binding.fabToWatchLater.setImageResource(R.drawable.watch_later_icon_selector)
        } else {
            setFloatButtonsState()
        }

        setTransitionAnimation()
        prepareMenuFabDialog()
        binding.fabDialogMenuProgressIndicator.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (requireActivity().isChangingConfigurations.not()) {
            // Принятие решения о добавлении/удалении фильма в избранном
            changeFavoriteMoviesList()
            changeWatchLaterMoviesList()
        }
        _binding = null
        disposableContainer.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        posterDownloadDisposable?.dispose()
    }

    private fun setFloatButtonsState() {
        checkFavoriteFloatButtonState()
        checkWatchLaterFloatButtonState()
    }

    private fun checkFavoriteFloatButtonState() = viewLifecycleOwner.lifecycleScope.launch {
        val disposable = viewModel.getFavoritesMovies?.subscribe { favoritesMovies ->
            val existedFavoriteDatabaseMovie: DatabaseMovie? = favoritesMovies.find {
                it.title == viewModel.movie.title && it.description == viewModel.movie.description
            }
            if (existedFavoriteDatabaseMovie != null) {
                viewModel.isFavoriteMovie = true
                binding.fabToFavorite.isSelected = true
                viewModel.isFavoriteButtonSelected = true
            } else {
                viewModel.isFavoriteMovie = false
                binding.fabToFavorite.isSelected = false
                viewModel.isFavoriteButtonSelected = false
            }
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)
        }
        disposableContainer.add(disposable ?: return@launch)
    }

    private fun checkWatchLaterFloatButtonState() = viewLifecycleOwner.lifecycleScope.launch {
        val disposable = viewModel.getWatchLaterMovies?.subscribe { watchLaterMovies ->
            val existedWatchLaterDatabaseMovie: DatabaseMovie? = watchLaterMovies.find {
                it.title == viewModel.movie.title && it.description == viewModel.movie.description
            }
            if (existedWatchLaterDatabaseMovie != null) {
                viewModel.isWatchLaterMovie = true
                binding.fabToWatchLater.isSelected = true
                viewModel.isWatchLaterButtonSelected = true
            } else {
                viewModel.isWatchLaterMovie = false
                binding.fabToWatchLater.isSelected = false
                viewModel.isWatchLaterButtonSelected = false
            }
            binding.fabToWatchLater.setImageResource(R.drawable.watch_later_icon_selector)
        }
        disposableContainer.add(disposable ?: return@launch)
    }

    private fun setFloatButtonOnClick() {
        binding.fabToFavorite.setOnClickListener {
            if (BuildConfig.DEMO) {
                if ((requireActivity() as MainActivity).checkDemoExpiredWithToast(R.string.details_fragment_fab_add_favorite_toast_demo_expired))
                    return@setOnClickListener
            }

            binding.fabToFavorite.isSelected = !binding.fabToFavorite.isSelected
            binding.fabToFavorite.setImageResource(R.drawable.favorite_icon_selector)
            viewModel.isFavoriteButtonSelected = binding.fabToFavorite.isSelected

            viewModel.changeFavoriteListImmediatelyIfPossible()

            Snackbar.make(requireContext(), binding.root,
                if (binding.fabToFavorite.isSelected) getString(R.string.details_fragment_fab_add_favorite)
                else getString(R.string.details_fragment_fab_remove_favorite),
                Snackbar.LENGTH_SHORT).show()
        }

        binding.fabToWatchLater.setOnClickListener {
            if (BuildConfig.DEMO) {
                if ((requireActivity() as MainActivity).checkDemoExpiredWithToast(R.string.details_fragment_fab_add_watch_later_toast_demo_expired))
                    return@setOnClickListener
            }

            if (checkNotificationPermission().not()) {
                requestNotificationPermission()
                return@setOnClickListener
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (viewModel.watchMovieNotification.checkIsChannelEnabledWithSnackbar(
                        requireActivity(),
                        binding.root
                ).not()) {
                    return@setOnClickListener
                }
            }

            fun handleClick(isButtonSelected: Boolean, scheduledDate: Long?) {
                binding.fabToWatchLater.isSelected = isButtonSelected
                viewModel.isWatchLaterButtonSelected = isButtonSelected

                viewModel.changeWatchLaterListImmediatelyIfPossible(requireContext(), scheduledDate)

                Snackbar.make(
                    requireContext(), binding.root,
                    if (viewModel.isWatchLaterButtonSelected)
                        getString(R.string.details_fragment_fab_add_watch_later)
                    else
                        getString(R.string.details_fragment_fab_remove_watch_later),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            if (viewModel.isWatchLaterButtonSelected.not()) {
                if (viewModel.fragmentThatLaunchedDetails != WatchLaterFragment::class.qualifiedName) {
                    DateTimePickerDialog.show(
                        activity = requireActivity(),
                        datePickerTitle = R.string.details_fragment_fab_add_watch_later_date_picker_title,
                        timePickerTitle = R.string.details_fragment_fab_add_watch_later_time_picker_title
                    ) { date ->
                        handleClick(isButtonSelected = true, scheduledDate = date)
                    }
                } else {
                    handleClick(isButtonSelected = true, scheduledDate = null)
                }
            } else {
                handleClick(isButtonSelected = false, scheduledDate = null)
            }
            binding.fabToWatchLater.setImageDrawable(
                AppCompatResources.getDrawable(requireContext(), R.drawable.watch_later_icon_selector)
            )
        }

        binding.fabDialogMenu.setOnClickListener {
            menuFabDialog?.show()
        }
    }

    private fun initializeContent() {
        if (!viewModel.isLowQualityPosterDownloaded) {
            binding.collapsingToolbarImage.apply {
                if (viewModel.movie.poster != null) {
                    Glide.with(this@DetailsFragment)
                        .load("${TmdbCommonConstants.IMAGES_URL}${TmdbCommonConstants.IMAGE_MEDIUM_SIZE}${viewModel.movie.poster}")
                        .placeholder(R.drawable.dummy_poster)
                        .apply(RequestOptions().dontTransform())
                        .onlyRetrieveFromCache(true)
                        .listener(object : RequestListener<Drawable>{  // Подождать полной загрузки из кэша и только потом делать перенос постера
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                startPostponedEnterTransition()
                                return false
                            }
                            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                startPostponedEnterTransition()  // Если запускать без паузы сразу transition, то будут глитчи из-за Glide
                                return false
                            }
                        })
                        .into(this)
                } else {
                    Glide.with(this@DetailsFragment)
                        .load(R.drawable.dummy_poster)
                        .apply(RequestOptions().dontTransform())
                        .into(this)
                    doOnPreDraw { startPostponedEnterTransition() }
                }
                transitionName = arguments?.getString(MainActivity.POSTER_TRANSITION_KEY)
            }
            viewModel.isLowQualityPosterDownloaded = true
        }
        binding.title.text = viewModel.movie.title
        binding.description.text = viewModel.movie.description
    }

    private suspend fun downloadHighResolutionPosterImage() = withContext(Dispatchers.IO) download@ {
        if (viewModel.movie.poster != null) {
            val downloadingImage = Glide.with(this@DetailsFragment)
                .load("${TmdbCommonConstants.IMAGES_URL}${TmdbCommonConstants.IMAGE_HIGH_SIZE}${viewModel.movie.poster}")
                .submit()  // Дальнейшее в этом методе просто для того, чтобы не использовать placeholder т.к. он глючит с drawable из вью
            val imageDrawable = try {
                downloadingImage.get()
            } catch (e: Exception) {
                return@download
            }
            withContext(Dispatchers.Main) {
                binding.collapsingToolbarImage.setImageDrawable(imageDrawable)
            }
        }
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
        if (viewModel.fragmentThatLaunchedDetails == FavoritesFragment::class.qualifiedName) {
            if (viewModel.isFavoriteMovie && !viewModel.isFavoriteButtonSelected) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    FavoritesFragment.FAVORITES_DETAILS_RESULT_KEY,
                    bundleOf(FavoritesFragment.MOVIE_NOW_NOT_FAVORITE_KEY to true)
                )
            }
        }
    }

    private fun changeWatchLaterMoviesList() {
        if (viewModel.fragmentThatLaunchedDetails == WatchLaterFragment::class.qualifiedName) {
            if (viewModel.isWatchLaterMovie && !viewModel.isWatchLaterButtonSelected) {
                requireActivity().supportFragmentManager.setFragmentResult(
                    WatchLaterFragment.WATCH_LATER_DETAILS_RESULT_KEY,
                    bundleOf(WatchLaterFragment.MOVIE_NOW_NOT_WATCH_LATER_KEY to true)
                )
                WorkRequests.cancelWatchLaterNotificationWork(requireContext(), viewModel.movie)
            }
        }
    }

    private fun setTransitionAnimation() {
        val sharedElementTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.poster_transition)
        sharedElementTransition.addListener(object : Transition.TransitionListener{
            override fun onTransitionStart(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionEnd(transition: Transition) {
                if (view == null) return
                viewLifecycleOwner.lifecycleScope.launch { downloadHighResolutionPosterImage() }
            }
        })
        sharedElementEnterTransition = sharedElementTransition
        postponeEnterTransition()

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
                addTarget(binding.fabDialogMenu)
            }
            this.duration = duration
            addTransition(appBarTransition)
            addTransition(movieInformationTransition)
            addTransition(fabTransition)
        }
    }

    private fun prepareMenuFabDialog() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.details_fragment_alert_dialog_title))
            .setView(R.layout.alert_dialog_content_for_fragment_details)
            .setNegativeButton(getString(R.string.details_fragment_alert_dialog_action_cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()
            .changeAppearanceToSamsungOneUI(Gravity.BOTTOM)

        initializeDialogButtons(alertDialog)

        menuFabDialog = alertDialog
    }

    private fun initializeDialogButtons(alertDialog: AlertDialog) {
        alertDialog.findViewById<Button>(R.id.alert_dialog_share_button)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(
                Intent.EXTRA_TEXT,
                getString(
                    R.string.details_fragment_fab_share_sending_text,
                    viewModel.movie.title, viewModel.movie.description
                )
            )
            startActivity(Intent.createChooser(intent, getString(R.string.details_fragment_fab_share_to)))

            menuFabDialog?.dismiss()
        }
        alertDialog.findViewById<Button>(R.id.alert_dialog_download_poster_button)?.setOnClickListener {
            performAsyncLoadOfPoster()

            menuFabDialog?.dismiss()
        }
    }

    private fun checkExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            permission == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            EXTERNAL_WRITE_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            )
            permission == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun registerRequestNotificationPermissionActivity() = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isNotificationGranted ->
        if (isNotificationGranted) {
            binding.fabToWatchLater.performClick()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionActivity.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun saveToGallery(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "${viewModel.movie.title.removeSingleQuote()}_poster")
                put(MediaStore.Images.Media.DISPLAY_NAME, viewModel.movie.title.removeSingleQuote())
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / DIVIDER_MILLISECONDS_TO_SECONDS)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MovieSearcherApp")
            }
            val contentResolver = requireActivity().contentResolver
            val url  = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw IOException("Failure on write to external storage")
            contentResolver.openOutputStream(url).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_COMPRESS_QUALITY, stream ?: return@use )
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                bitmap,
                viewModel.movie.title.removeSingleQuote(),
                viewModel.movie.description.removeSingleQuote()
            )
        }
    }

    fun performAsyncLoadOfPoster() {
        if (!checkExternalStoragePermission()) {
            requestExternalStoragePermission()
            return
        }
        binding.fabDialogMenuProgressIndicator.show()

        posterDownloadDisposable = Completable.fromSingle<Unit> { observer ->
            try {
                saveToGallery(
                    viewModel.loadMoviePoster(
                        "${TmdbCommonConstants.IMAGES_URL}${TmdbCommonConstants.IMAGE_FULL_SIZE}${viewModel.movie.poster}"
                    )
                )
                observer.onSuccess(Unit)
            } catch (e: Exception) {
                observer.onError(e)
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                success@ {
                    if (_binding?.root == null) return@success

                    Snackbar.make(
                        binding.root,
                        R.string.details_fragment_poster_download_success,
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.details_fragment_poster_download_success_action) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.type = "image/*"
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.show()

                    binding.fabDialogMenuProgressIndicator.hide()

                    posterDownloadDisposable?.dispose()
                },
                error@ { error ->
                    if (_binding?.root == null) return@error

                    Snackbar.make(
                        binding.root,
                        "${getString(R.string.details_fragment_poster_download_failure)}\n${error.localizedMessage}",
                        Snackbar.LENGTH_LONG
                    ).show()

                    binding.fabDialogMenuProgressIndicator.hide()

                    posterDownloadDisposable?.dispose()
                }
            )
    }

    private fun String.removeSingleQuote() = replace("'", "")


    companion object {
        const val EXTERNAL_WRITE_PERMISSION_REQUEST_CODE = 20

        const val KEY_SEPARATE_DETAILS_FRAGMENT = "SEPARATE_DETAILS_FRAGMENT"

        private const val TOOLBAR_SCRIM_VISIBLE_TRIGGER_POSITION_MULTIPLIER = 2

        private const val DIVIDER_MILLISECONDS_TO_SECONDS = 1000
        private const val JPEG_COMPRESS_QUALITY = 100
    }
}
