package com.tony.coreui.sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tony.coreui.sample.presentation.navigation.SampleNavigator

@Composable
fun CoreUiSampleApp() {
    val appContainer = remember { SampleAppContainer() }

    SampleNavigator(appContainer = appContainer)
}
