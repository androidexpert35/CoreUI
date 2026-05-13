package com.tony.coreui.presentation.components.basescreen

/**
 * Rendering policy for [AppBaseScreen].
 *
 * These options keep the current defaults intact while allowing hosts to relax the built-in
 * behavior when they need more control over layout continuity and system chrome ownership.
 *
 * @param applySystemAppearance when `true`, [SystemAppearance] is applied automatically.
 * @param hideContentOnDefaultLoading when `true`, [BaseLoadingType.DEFAULT] replaces content while
 * loading.
 * @param keepContentVisibleOnError when `true`, the latest rendered content remains underneath
 * full-screen error treatments when data is available.
 */
data class BaseScreenRenderPolicy(
    val applySystemAppearance: Boolean = true,
    val hideContentOnDefaultLoading: Boolean = true,
    val keepContentVisibleOnError: Boolean = true
)
