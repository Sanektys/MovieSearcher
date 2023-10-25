package com.sandev.moviesearcher.data

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.sandev.moviesearcher.R


class RemoteConfigProvider {

    companion object {
        fun getInstance(): FirebaseRemoteConfig {
            val settings = remoteConfigSettings {
                fetchTimeoutInSeconds = FETCH_TIMEOUT
                minimumFetchIntervalInSeconds = FETCH_INTERVAL  // Default interval is 12 hours
            }
            return Firebase.remoteConfig.apply {
                setConfigSettingsAsync(settings)
                setDefaultsAsync(R.xml.remote_config_defaults)
            }
        }

        private const val FETCH_INTERVAL = 5L
        private const val FETCH_TIMEOUT = 10L
    }
}