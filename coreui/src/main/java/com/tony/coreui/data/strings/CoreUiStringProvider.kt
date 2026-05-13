package com.tony.coreui.data.strings

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.tony.coreui.data.strings.CoreUiStringProvider.init

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
     *
     * @param context any context from which an application context can be derived.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Returns `true` when the provider has been initialized.
     *
     * @return `true` when [init] has already been called.
     */
    fun isInitialized(): Boolean = appContext != null

    /**
     * Resolves a string resource with optional formatting arguments.
     *
     * @param id string resource identifier.
     * @param formatArgs optional formatting arguments passed to [Context.getString].
     * @return localized string value resolved from [id].
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
     * @param id plural resource identifier.
     * @param quantity quantity used to select the correct plural form.
     * @param formatArgs optional formatting arguments passed to the resolved plural string.
     * @return localized plural string value resolved from [id].
     * @throws IllegalStateException when the provider has not been initialized yet.
     */
    fun getPlural(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
        val context = appContext ?: error(
            "CoreUiStringProvider is not initialized. Call CoreUiStringProvider.init(context) from Application."
        )
        return context.resources.getQuantityString(id, quantity, *formatArgs)
    }
}
