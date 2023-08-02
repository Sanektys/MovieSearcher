package com.sandev.moviesearcher.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import com.sandev.moviesearcher.R


class PreferenceSwitch(context: Context, attr: AttributeSet) : ConstraintLayout(context, attr) {

    val title: MaterialTextView
    val description: MaterialTextView
    val switch: MaterialSwitch

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_switch_preference, this, true)

        title = getChildAt(0) as MaterialTextView
        description = getChildAt(1) as MaterialTextView
        switch = getChildAt(2) as MaterialSwitch

        val attributes = context.theme.obtainStyledAttributes(attr, R.styleable.PreferenceSwitch, 0 ,0)
        try {
            title.text = attributes.getString(R.styleable.PreferenceSwitch_preference_title)
            description.text = attributes.getString(R.styleable.PreferenceSwitch_preference_description)
        } finally {
            attributes.recycle()
        }

        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        setBackgroundResource(typedValue.resourceId)
        isFocusable = true
        isClickable = true
    }
}