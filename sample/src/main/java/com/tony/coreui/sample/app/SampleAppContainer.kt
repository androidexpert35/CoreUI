package com.tony.coreui.sample.app

import com.tony.coreui.data.navigation.NavigationManagerImpl
import com.tony.coreui.presentation.error.UiErrorMapper
import com.tony.coreui.presentation.navigation.NavigationManager
import com.tony.coreui.sample.data.repository.FakeShowcaseRepository
import com.tony.coreui.sample.domain.repository.ShowcaseRepository
import com.tony.coreui.sample.presentation.feature.detail.SampleDetailUiErrorMapper

/**
 * Lightweight manual dependency container for the sample app.
 *
 * The sample intentionally avoids a DI framework so consumers can see the smallest viable setup.
 */
class SampleAppContainer {
    val navigationManager: NavigationManager = NavigationManagerImpl()
    val showcaseRepository: ShowcaseRepository = FakeShowcaseRepository()
    val detailUiErrorMapper: UiErrorMapper = SampleDetailUiErrorMapper()
}
