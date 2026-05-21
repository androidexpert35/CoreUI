package com.tony.coreui.sample.presentation.navigation

import com.tony.coreui.presentation.navigation.route.enumQueryArgument
import com.tony.coreui.presentation.navigation.route.longPathArgument
import com.tony.coreui.presentation.navigation.route.route
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection

/** Central registry of typed routes used by the sample app. */
object SampleRoutes {
    val libraryFilter = enumQueryArgument(
        name = "filter",
        enumClass = DemoFilter::class.java,
        defaultValue = DemoFilter.ALL
    )

    val library = route(
        baseRoute = "library",
        libraryFilter
    )

    val albumId = longPathArgument("albumId")
    val detailSection = enumQueryArgument(
        name = "section",
        enumClass = DetailSection::class.java,
        defaultValue = DetailSection.OVERVIEW
    )

    val detail = route(
        baseRoute = "album",
        albumId,
        detailSection
    )
}
