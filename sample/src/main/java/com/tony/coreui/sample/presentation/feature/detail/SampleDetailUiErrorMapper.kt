package com.tony.coreui.sample.presentation.feature.detail

import com.tony.coreui.data.strings.CoreUiStringProvider
import com.tony.coreui.data.strings.StringResolver
import com.tony.coreui.domain.resource.ResourceError
import com.tony.coreui.presentation.error.DefaultUiErrorMapper
import com.tony.coreui.presentation.error.UiErrorMapper
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.sample.R

class SampleDetailUiErrorMapper(
    private val stringResolver: StringResolver = CoreUiStringProvider
) : UiErrorMapper {

    private val delegate = DefaultUiErrorMapper(stringResolver)

    override fun map(errorObject: Any, retryAction: (() -> Unit)?): UIError = when (errorObject) {
        is ResourceError -> mapResourceError(errorObject, retryAction)
        else -> delegate.map(errorObject, retryAction)
    }

    override fun mapResourceError(resource: ResourceError?, retryAction: (() -> Unit)?): UIError {
        return when (resource) {
            is ResourceError.ServiceError -> delegate.mapResourceError(resource, retryAction).copy(
                title = resolveString(R.string.sample_detail_mapper_service_title),
                message = resolveString(R.string.sample_detail_mapper_service_body),
                displayMode = UIErrorDisplayMode.FULL_SCREEN
            )
            is ResourceError.LogicError ->
                if (resource.errorCode == "STALE_DEMO") {
                    delegate.mapResourceError(resource, retryAction).copy(
                        title = resolveString(R.string.sample_detail_mapper_warning_title),
                        message = resolveString(R.string.sample_detail_mapper_warning_body),
                        displayMode = UIErrorDisplayMode.NONE
                    )
                } else {
                    delegate.mapResourceError(resource, retryAction)
                }
            else -> delegate.mapResourceError(resource, retryAction)
        }
    }

    private fun resolveString(id: Int, vararg args: Any): String {
        return try {
            stringResolver.get(id, *args)
        } catch (_: Throwable) {
            id.toString()
        }
    }
}
