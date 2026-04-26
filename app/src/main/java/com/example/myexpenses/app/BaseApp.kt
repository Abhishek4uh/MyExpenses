package com.example.myexpenses.app

import android.app.Application
import com.example.myexpenses.core.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BaseApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        NotificationHelper.createChannels(this)
    }
}