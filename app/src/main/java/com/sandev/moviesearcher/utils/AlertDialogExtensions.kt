package com.sandev.moviesearcher.utils

import android.content.DialogInterface
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.sandev.moviesearcher.R


fun AlertDialog.changeAppearanceToSamsungOneUI(gravity: Int): AlertDialog {
    window?.setGravity(gravity)
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