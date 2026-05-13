package com.tony.coreui.data.strings

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Resolves localized strings for library collaborators that run outside composable scope.
 *
 * Consumers can keep using [CoreUiStringProvider] as a convenient process-wide default, or inject
 * a dedicated implementation such as [AndroidStringResolver] for stricter lifecycle control.
 */
interface StringResolver {
    /**
     * Resolves a string resource with optional formatting arguments.
     */
    fun get(@StringRes id: Int, vararg formatArgs: Any): String

    /**
     * Resolves a plural string resource with optional formatting arguments.
     */
    fun getPlural(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String
}

/**
 * Context-backed [StringResolver] that does not rely on global mutable state.
 */
class AndroidStringResolver(context: Context) : StringResolver {
    private val appContext = context.applicationContext

    override fun get(@StringRes id: Int, vararg formatArgs: Any): String {
        return if (formatArgs.isEmpty()) {
            appContext.getString(id)
        } else {
            appContext.getString(id, *formatArgs)
        }
    }

    override fun getPlural(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
        return appContext.resources.getQuantityString(id, quantity, *formatArgs)
    }
}
