package com.tony.coreui.presentation.components.basescreen

/**
 * Defines how loading feedback should be rendered by [AppBaseScreen].
 *
 * [DEFAULT] replaces the main content while loading, [OVERLAY] keeps the current content visible
 * and draws a loading layer above it, and [NONE] suppresses all built-in loading UI.
 */
enum class BaseLoadingType {
    DEFAULT,
    OVERLAY,
    NONE
}
