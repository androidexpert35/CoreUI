package com.tony.coreui.sample.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tony.coreui.sample.presentation.theme.CoreUiSampleTheme

/** Single-activity host for the runnable CoreUI sample app. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CoreUiSampleTheme {
                CoreUiSampleApp()
            }
        }
    }
}
