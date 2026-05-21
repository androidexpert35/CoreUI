package com.tony.coreui.sample.presentation.feature.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tony.coreui.presentation.components.basescreen.AppBaseScreen
import com.tony.coreui.presentation.components.basescreen.BaseLoadingType
import com.tony.coreui.presentation.components.basescreen.BaseScreenRenderPolicy
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DetailSection
import kotlinx.coroutines.flow.collectLatest

/**
 * Demonstrates the more flexible side of `AppBaseScreen`.
 *
 * Compared to the list screen, this variant opts into overlay loading, custom full-screen error
 * rendering, and `contentWithState` so the host can react to `UIErrorDisplayMode.NONE`.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailScreen(
    viewModel: DetailViewModel,
    albumId: Long,
    initialSection: DetailSection
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is DetailEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Column {
                        Text(text = stringResource(R.string.sample_detail_title))
                        Text(
                            text = stringResource(
                                R.string.sample_detail_route_note,
                                albumId,
                                initialSection.label()
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = { viewModel.onEvent(DetailEvent.NavigateUpRequested) }) {
                        Text(text = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val renderContent = @Composable { model: DetailUiModel, state: UIState<DetailUiModel> ->
                DetailContent(
                    model = model,
                    uiState = state,
                    initialSection = initialSection,
                    onRefresh = { viewModel.onEvent(DetailEvent.RefreshRequested) },
                    onServiceFailure = { viewModel.onEvent(DetailEvent.ServiceFailureRequested) },
                    onHostWarning = { viewModel.onEvent(DetailEvent.HostWarningRequested) },
                    onOpenRelated = { viewModel.onEvent(DetailEvent.RelatedAlbumRequested) },
                    onReset = { viewModel.onEvent(DetailEvent.ResetToDefaultsRequested) }
                )
            }

            AppBaseScreen(
                uiState = uiState,
                loadingType = BaseLoadingType.OVERLAY,
                renderPolicy = BaseScreenRenderPolicy(keepContentVisibleOnError = false),
                onErrorDialogDismiss = viewModel::dismissErrorPopup,
                errorScreen = { error ->
                    DetailServiceErrorScreen(
                        error = error,
                        onRetry = { viewModel.onEvent(DetailEvent.RetryInitialLoadRequested) },
                        onReset = { viewModel.onEvent(DetailEvent.ResetToDefaultsRequested) }
                    )
                },
                contentWithState = renderContent
            ) { model ->
                renderContent(model, uiState)
            }
        }
    }
}
