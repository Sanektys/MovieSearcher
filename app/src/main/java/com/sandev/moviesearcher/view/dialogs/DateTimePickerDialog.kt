package com.sandev.moviesearcher.view.dialogs

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import java.util.Calendar


class DateTimePickerDialog {

    companion object {
        fun show(activity: FragmentActivity, @StringRes datePickerTitle: Int?,
                 @StringRes timePickerTitle: Int?, onDateTimeSet: (date: Long) -> Unit) {
            val calendar = Calendar.getInstance()

            val timePicker = MaterialTimePicker.Builder()
                .setTitleText(if (timePickerTitle != null) activity.getString(timePickerTitle) else null)
                .setTimeFormat(if (DateFormat.is24HourFormat(activity)) CLOCK_24H else CLOCK_12H)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .build()

            MaterialDatePicker.Builder.datePicker()
                .setTitleText(if (datePickerTitle != null) activity.getString(datePickerTitle) else null)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(
                    CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointForward.now())
                        .build()
                )
                .build()
                .apply {
                    addOnPositiveButtonClickListener { date ->
                        calendar.timeInMillis = date

                        with(timePicker) {
                            addOnPositiveButtonClickListener {
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)

                                onDateTimeSet.invoke(calendar.timeInMillis)
                            }
                            // После календаря сразу запускать диалог с часами
                            show(activity.supportFragmentManager, null)
                        }
                    }
                }
                .show(activity.supportFragmentManager, null)
        }
    }
}