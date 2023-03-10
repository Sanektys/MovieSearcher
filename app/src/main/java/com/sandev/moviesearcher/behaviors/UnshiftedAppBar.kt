package com.sandev.moviesearcher.behaviors

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.appbar.AppBarLayout


class UnshiftedAppBar(context: Context, attr: AttributeSet) : AppBarLayout.Behavior(context, attr) {
    init {
        setDragCallback(object : DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout) = false
        })
    }
}