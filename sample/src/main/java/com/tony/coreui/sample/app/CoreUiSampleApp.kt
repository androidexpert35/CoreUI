package com.tony.coreui.sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tony.coreui.sample.presentation.navigation.SampleNavigator

/** Root composable that wires the shared app container into the sample navigator. */
@Composable
fun CoreUiSampleApp() {
    val appContainer = remember { SampleAppContainer() }

    SampleNavigator(appContainer = appContainer)
}
