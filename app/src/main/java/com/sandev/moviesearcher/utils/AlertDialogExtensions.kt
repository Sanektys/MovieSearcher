package com.sandev.moviesearcher.utils

import android.content.DialogInterface
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.sandev.moviesearcher.R


fun AlertDialog.changeAppearanceToSamsungOneUI(): AlertDialog {
    window?.setGravity(Gravity.BOTTOM)
    window?.setBackgroundDrawableResource(R.drawable.alert_dialog_background)

    show()  // Без show() getButton выдаст null
    val negativeButton = getButton(DialogInterface.BUTTON_NEGATIVE)
    dismiss()

    val buttonNewLayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    negativeButton.layoutParams = buttonNewLayoutParams

    return this
}