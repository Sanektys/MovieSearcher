package com.sandev.moviesearcher.utils.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BatteryBroadcastReceiver(
    private val onBatteryLow: () -> Unit,
    private val onBatteryOkay: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_LOW) {
            onBatteryLow.invoke()
        } else if (intent?.action == Intent.ACTION_BATTERY_OKAY) {
            onBatteryOkay.invoke()
        }
    }
}