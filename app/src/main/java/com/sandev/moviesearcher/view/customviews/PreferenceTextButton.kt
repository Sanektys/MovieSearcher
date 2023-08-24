package com.sandev.moviesearcher.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textview.MaterialTextView
import com.sandev.moviesearcher.R


class PreferenceTextButton(context: Context, attr: AttributeSet) : ConstraintLayout(context, attr) {

    val title: MaterialTextView
    val description: MaterialTextView

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_text_button_preference, this, true)

        title = getChildAt(0) as MaterialTextView
        description = getChildAt(1) as MaterialTextView

        val attributes = context.theme.obtainStyledAttributes(attr, R.styleable.PreferenceTextButton, 0 ,0)
        try {
            title.text = attributes.getString(R.styleable.PreferenceTextButton_preference_title)
            description.text = attributes.getString(R.styleable.PreferenceTextButton_preference_description)
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