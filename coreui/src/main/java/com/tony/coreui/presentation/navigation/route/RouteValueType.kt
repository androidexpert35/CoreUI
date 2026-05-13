package com.tony.coreui.presentation.navigation.route

import android.os.Bundle
import androidx.navigation.NavType
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Strategy object used by [RouteArgument] to serialize route values and read them back from the
 * Navigation runtime.
 */
interface RouteValueType<T> {
    val navType: NavType<*>

    fun serialize(value: T): String

    fun read(bundle: Bundle, key: String): T?
}

private class DefaultRouteValueType<T>(
    override val navType: NavType<*>,
    private val serializer: (T) -> String,
    private val reader: (Bundle, String) -> T?
) : RouteValueType<T> {
    override fun serialize(value: T): String = serializer(value)

    override fun read(bundle: Bundle, key: String): T? = reader(bundle, key)
}

/**
 * Built-in value types for the default CoreUI navigation route helpers.
 */
object RouteValueTypes {
    val String: RouteValueType<kotlin.String> = DefaultRouteValueType(
        navType = NavType.StringType,
        serializer = { value -> encodeRouteText(value) },
        reader = Bundle::getString
    )

    val Long: RouteValueType<kotlin.Long> = DefaultRouteValueType(
        navType = NavType.LongType,
        serializer = { value -> value.toString() },
        reader = { bundle, key -> if (bundle.containsKey(key)) bundle.getLong(key) else null }
    )

    val Int: RouteValueType<kotlin.Int> = DefaultRouteValueType(
        navType = NavType.IntType,
        serializer = { value -> value.toString() },
        reader = { bundle, key -> if (bundle.containsKey(key)) bundle.getInt(key) else null }
    )

    val Boolean: RouteValueType<kotlin.Boolean> = DefaultRouteValueType(
        navType = NavType.BoolType,
        serializer = { value -> value.toString() },
        reader = { bundle, key -> if (bundle.containsKey(key)) bundle.getBoolean(key) else null }
    )

    val Float: RouteValueType<kotlin.Float> = DefaultRouteValueType(
        navType = NavType.FloatType,
        serializer = { value -> value.toString() },
        reader = { bundle, key -> if (bundle.containsKey(key)) bundle.getFloat(key) else null }
    )

    fun <T : Enum<T>> enum(enumClass: Class<T>): RouteValueType<T> = DefaultRouteValueType(
        navType = NavType.StringType,
        serializer = { value -> encodeRouteText(value.name) },
        reader = { bundle, key ->
            bundle.getString(key)?.let { raw ->
                java.lang.Enum.valueOf(enumClass, raw)
            }
        }
    )

    fun <T> custom(
        navType: NavType<*>,
        serializer: (T) -> String,
        reader: (Bundle, String) -> T?
    ): RouteValueType<T> = DefaultRouteValueType(
        navType = navType,
        serializer = serializer,
        reader = reader
    )
}

private fun encodeRouteText(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20")
