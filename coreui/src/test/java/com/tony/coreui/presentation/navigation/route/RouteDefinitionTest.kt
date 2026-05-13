package com.tony.coreui.presentation.navigation.route

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteDefinitionTest {

    private val albumIdArgument = longPathArgument("albumId")
    private val highlightArgument = booleanQueryArgument(
        name = "highlight",
        defaultValue = false
    )
    private val artistArgument = stringQueryArgument(
        name = "artistName",
        nullable = true
    )

    private val albumRoute = route(
        baseRoute = "album",
        albumIdArgument,
        highlightArgument,
        artistArgument
    )

    @Test
    fun routePattern_includesPathAndQueryArguments() {
        assertEquals(
            "album/{albumId}?highlight={highlight}&artistName={artistName}",
            albumRoute.routePattern
        )
    }

    @Test
    fun createRoute_serializesRegisteredArgumentsOnly() {
        val result = albumRoute.createRoute(
            albumIdArgument with 42L,
            highlightArgument with true,
            artistArgument with "Boards of Canada"
        )

        assertEquals(
            "album/42?highlight=true&artistName=Boards%20of%20Canada",
            result
        )
    }

    @Test
    fun createRoute_omitsOptionalQueryArgumentsWhenNotProvided() {
        val result = albumRoute.createRoute(
            albumIdArgument with 42L
        )

        assertEquals("album/42?highlight=false", result)
    }
}
