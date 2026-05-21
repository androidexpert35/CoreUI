package com.tony.coreui.sample.app

import android.app.Application
import com.tony.coreui.data.strings.CoreUiStringProvider

/** Initializes process-wide CoreUI services needed by the sample app. */
class CoreUiSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
