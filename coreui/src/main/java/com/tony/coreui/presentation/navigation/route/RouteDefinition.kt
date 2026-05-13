package com.tony.coreui.presentation.navigation.route

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry

/**
 * Strongly modeled route contract used by the default CoreUI navigation stack.
 *
 * A route definition owns its path pattern, query arguments, validation rules, and helpers for
 * building concrete route strings.
 */
class RouteDefinition(
    val baseRoute: String,
    arguments: List<RouteArgument<*>> = emptyList()
) {
    val pathArguments: List<RouteArgument<*>> = arguments.filter {
        it.location == RouteArgumentLocation.PATH
    }

    val queryArguments: List<RouteArgument<*>> = arguments.filter {
        it.location == RouteArgumentLocation.QUERY
    }

    val allArguments: List<RouteArgument<*>> = arguments.toList()

    val routePattern: String = buildString {
        append(baseRoute)
        pathArguments.forEach { argument ->
            append("/")
            append(argument.patternSegment())
        }
        if (queryArguments.isNotEmpty()) {
            append("?")
            append(
                queryArguments.joinToString("&") { argument ->
                    "${argument.name}={${argument.name}}"
                }
            )
        }
    }

    val navArguments: List<NamedNavArgument> = allArguments.map(RouteArgument<*>::asNamedNavArgument)

    init {
        require(baseRoute.isNotBlank()) { "RouteDefinition baseRoute cannot be blank." }
        val duplicateNames = allArguments
            .groupBy(RouteArgument<*>::name)
            .filterValues { it.size > 1 }
            .keys
        require(duplicateNames.isEmpty()) {
            "RouteDefinition '$baseRoute' contains duplicate arguments: ${duplicateNames.joinToString()}."
        }
    }

    fun createRoute(vararg values: RouteValue<*>): String = createRoute(values.toList())

    fun createRoute(values: Iterable<RouteValue<*>>): String {
        val valuesByName = values.associateBy { it.argument.name }

        val unknownNames = valuesByName.keys - allArguments.map { it.name }.toSet()
        require(unknownNames.isEmpty()) {
            "Unknown arguments for route '$baseRoute': ${unknownNames.joinToString()}."
        }

        val renderedPath = buildString {
            append(baseRoute)
            pathArguments.forEach { argument ->
                val routeValue = valuesByName[argument.name]
                    ?: error("Missing required path argument '${argument.name}' for route '$baseRoute'.")
                append("/")
                append(serialize(argument, routeValue.value))
            }
        }

        val queryParts = queryArguments.mapNotNull { argument ->
            val routeValue = valuesByName[argument.name]
            val value = routeValue?.value ?: argument.defaultValue
            when {
                value != null -> "${argument.name}=${serialize(argument, value)}"
                argument.nullable || argument.defaultValue != null -> null
                else -> error("Missing required query argument '${argument.name}' for route '$baseRoute'.")
            }
        }

        return if (queryParts.isEmpty()) renderedPath else "$renderedPath?${queryParts.joinToString("&")}"
    }

    fun contains(argument: RouteArgument<*>): Boolean = allArguments.any { it.name == argument.name }

    fun <T> requireArgument(backStackEntry: NavBackStackEntry, argument: RouteArgument<T>): T {
        return getArgument(backStackEntry, argument)
            ?: error("Argument '${argument.name}' was missing from route '$baseRoute'.")
    }

    fun <T> getArgument(backStackEntry: NavBackStackEntry, argument: RouteArgument<T>): T? {
        require(contains(argument)) {
            "Argument '${argument.name}' is not registered on route '$baseRoute'."
        }
        val bundle = backStackEntry.arguments ?: return argument.defaultValue
        return argument.valueType.read(bundle, argument.name) ?: argument.defaultValue
    }

    override fun toString(): String = routePattern

    @Suppress("UNCHECKED_CAST")
    private fun serialize(argument: RouteArgument<*>, value: Any?): String {
        if (value == null) {
            error("Argument '${argument.name}' on route '$baseRoute' cannot be null here.")
        }
        return (argument.valueType as RouteValueType<Any>).serialize(value)
    }
}

fun route(
    baseRoute: String,
    vararg arguments: RouteArgument<*>
): RouteDefinition = RouteDefinition(
    baseRoute = baseRoute,
    arguments = arguments.toList()
)
