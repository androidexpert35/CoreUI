package com.tony.coreui.data.strings

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Process-wide string resolver for view models and other non-composable collaborators.
 *
 * Library consumers should initialize this provider once from `Application.onCreate()` before
 * invoking APIs that need string resolution outside a composable scope.
 */
object CoreUiStringProvider {

    @Volatile
    private var appContext: Context? = null

    /**
     * Initializes the provider with an application-scoped [context].
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Returns `true` when the provider has been initialized.
     */
    fun isInitialized(): Boolean = appContext != null

    /**
     * Resolves a string resource with optional formatting arguments.
     *
     * @throws IllegalStateException when the provider has not been initialized yet.
     */
    fun get(@StringRes id: Int, vararg formatArgs: Any): String {
        val context = appContext ?: error(
            "CoreUiStringProvider is not initialized. Call CoreUiStringProvider.init(context) from Application."
        )
        return if (formatArgs.isEmpty()) {
            context.getString(id)
        } else {
            context.getString(id, *formatArgs)
        }
    }

    /**
     * Resolves a plural string resource with optional formatting arguments.
     *
     * @throws IllegalStateException when the provider has not been initialized yet.
     */
    fun getPlural(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
        val context = appContext ?: error(
            "CoreUiStringProvider is not initialized. Call CoreUiStringProvider.init(context) from Application."
        )
        return context.resources.getQuantityString(id, quantity, *formatArgs)
    }
}
