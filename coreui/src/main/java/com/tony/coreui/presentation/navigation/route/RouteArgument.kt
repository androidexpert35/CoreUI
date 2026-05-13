package com.tony.coreui.presentation.navigation.route

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument

/**
 * Describes a single argument in a route definition.
 *
 * The same argument instance can be reused when building concrete routes and when reading values
 * back from a `NavBackStackEntry`, which keeps route declarations and usage aligned.
 */
data class RouteArgument<T>(
    val name: String,
    val location: RouteArgumentLocation,
    val valueType: RouteValueType<T>,
    val nullable: Boolean = false,
    val defaultValue: T? = null
) {
    init {
        require(name.isNotBlank()) { "Route argument names cannot be blank." }
        require(location == RouteArgumentLocation.QUERY || (!nullable && defaultValue == null)) {
            "Path arguments must be required values."
        }
    }

    internal fun patternSegment(): String = "{${name}}"

    internal fun asNamedNavArgument(): NamedNavArgument {
        val argument = this
        return navArgument(name) {
            type = argument.valueType.navType
            nullable = argument.nullable
            if (argument.defaultValue != null) {
                defaultValue = argument.defaultValue
            }
        }
    }
}

/**
 * Concrete value paired with a [RouteArgument] when building a route string.
 */
data class RouteValue<T>(
    val argument: RouteArgument<T>,
    val value: T?
)

/**
 * Convenience operator for building a [RouteValue] with the same [RouteArgument] instance used in
 * the route definition.
 */
infix fun <T> RouteArgument<T>.with(value: T?): RouteValue<T> = RouteValue(argument = this, value = value)

/**
 * Indicates where a route argument appears in a route string.
 */
enum class RouteArgumentLocation {
    PATH,
    QUERY
}

fun stringPathArgument(name: String): RouteArgument<String> =
    RouteArgument(name = name, location = RouteArgumentLocation.PATH, valueType = RouteValueTypes.String)

fun stringQueryArgument(
    name: String,
    nullable: Boolean = false,
    defaultValue: String? = null
): RouteArgument<String> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.String,
    nullable = nullable,
    defaultValue = defaultValue
)

fun longPathArgument(name: String): RouteArgument<Long> =
    RouteArgument(name = name, location = RouteArgumentLocation.PATH, valueType = RouteValueTypes.Long)

fun longQueryArgument(
    name: String,
    nullable: Boolean = false,
    defaultValue: Long? = null
): RouteArgument<Long> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.Long,
    nullable = nullable,
    defaultValue = defaultValue
)

fun intPathArgument(name: String): RouteArgument<Int> =
    RouteArgument(name = name, location = RouteArgumentLocation.PATH, valueType = RouteValueTypes.Int)

fun intQueryArgument(
    name: String,
    nullable: Boolean = false,
    defaultValue: Int? = null
): RouteArgument<Int> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.Int,
    nullable = nullable,
    defaultValue = defaultValue
)

fun booleanPathArgument(name: String): RouteArgument<Boolean> =
    RouteArgument(name = name, location = RouteArgumentLocation.PATH, valueType = RouteValueTypes.Boolean)

fun booleanQueryArgument(
    name: String,
    nullable: Boolean = false,
    defaultValue: Boolean? = null
): RouteArgument<Boolean> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.Boolean,
    nullable = nullable,
    defaultValue = defaultValue
)

fun floatPathArgument(name: String): RouteArgument<Float> =
    RouteArgument(name = name, location = RouteArgumentLocation.PATH, valueType = RouteValueTypes.Float)

fun floatQueryArgument(
    name: String,
    nullable: Boolean = false,
    defaultValue: Float? = null
): RouteArgument<Float> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.Float,
    nullable = nullable,
    defaultValue = defaultValue
)

fun <T : Enum<T>> enumPathArgument(
    name: String,
    enumClass: Class<T>
): RouteArgument<T> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.PATH,
    valueType = RouteValueTypes.enum(enumClass)
)

fun <T : Enum<T>> enumQueryArgument(
    name: String,
    enumClass: Class<T>,
    nullable: Boolean = false,
    defaultValue: T? = null
): RouteArgument<T> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = RouteValueTypes.enum(enumClass),
    nullable = nullable,
    defaultValue = defaultValue
)

fun <T> customPathArgument(
    name: String,
    valueType: RouteValueType<T>
): RouteArgument<T> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.PATH,
    valueType = valueType
)

fun <T> customQueryArgument(
    name: String,
    valueType: RouteValueType<T>,
    nullable: Boolean = false,
    defaultValue: T? = null
): RouteArgument<T> = RouteArgument(
    name = name,
    location = RouteArgumentLocation.QUERY,
    valueType = valueType,
    nullable = nullable,
    defaultValue = defaultValue
)
