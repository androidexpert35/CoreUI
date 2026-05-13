package com.tony.coreui.navigation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationManagerImplTest {

    @Test
    fun navigate_emitsCommandAndUpdatesCurrentRoute() = runTest {
        val manager = NavigationManagerImpl()
        val commands = async(UnconfinedTestDispatcher(testScheduler)) {
            manager.navigationCommands.take(1).toList()
        }

        manager.navigate(
            route = "details/42",
            options = NavigationOptions(launchSingleTop = true)
        )
        advanceUntilIdle()

        assertEquals("details/42", manager.currentRoute.value)
        assertEquals(
            listOf(
                NavigationCommand.Navigate(
                    route = "details/42",
                    options = NavigationOptions(launchSingleTop = true)
                )
            ),
            commands.await()
        )
    }

    @Test
    fun popBackStack_emitsPopCommandWithoutChangingCurrentRoute() = runTest {
        val manager = NavigationManagerImpl()
        manager.onRouteChanged("home")
        val commands = async(UnconfinedTestDispatcher(testScheduler)) {
            manager.navigationCommands.take(1).toList()
        }

        manager.popBackStack(route = null, inclusive = false)
        advanceUntilIdle()

        assertEquals("home", manager.currentRoute.value)
        assertEquals(
            listOf(NavigationCommand.PopBackStack(route = null, inclusive = false)),
            commands.await()
        )
    }

    @Test
    fun onRouteChanged_acceptsNullRoute() {
        val manager = NavigationManagerImpl()

        manager.onRouteChanged(null)

        assertNull(manager.currentRoute.value)
    }
}
