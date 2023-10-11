package com.sandev.moviesearcher.view

import android.view.View
import androidx.annotation.StringRes


fun MainActivity.showDemoInfoScreen(view: View, isAnimated: Boolean, okButtonCallback: (() -> Unit)? = null) {}

fun MainActivity.checkDemoExpired(): Boolean = false

fun MainActivity.checkDemoExpiredWithToast(@StringRes featureExpiredMessage: Int): Boolean = false