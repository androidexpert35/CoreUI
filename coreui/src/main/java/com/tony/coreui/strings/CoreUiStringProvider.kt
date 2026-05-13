package com.tony.coreui.strings

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Process-wide string resolver for view models and other non-composable code.
 */
object CoreUiStringProvider {

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun isInitialized(): Boolean = appContext != null

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

    fun getPlural(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
        val context = appContext ?: error(
            "CoreUiStringProvider is not initialized. Call CoreUiStringProvider.init(context) from Application."
        )
        return context.resources.getQuantityString(id, quantity, *formatArgs)
    }
}
