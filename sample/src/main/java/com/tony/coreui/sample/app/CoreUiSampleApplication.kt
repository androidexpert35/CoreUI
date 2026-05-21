package com.tony.coreui.sample.app

import android.app.Application
import com.tony.coreui.data.strings.CoreUiStringProvider

class CoreUiSampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreUiStringProvider.init(this)
    }
}
