package com.tony.coreui.sample.presentation.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.tony.coreui.presentation.components.basescreen.ErrorDialogConfig
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode
import kotlinx.coroutines.flow.collectLatest

/**
 * Demonstrates `AppBaseScreen` in its most convenient form.
 *
 * This screen intentionally sticks close to the defaults so consumers can see how little feature
 * code is required when they accept the library's built-in loading, dialog-error, and empty-state
 * behavior.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LibraryScreen(
    viewModel: LibraryViewModel,
    initialFilter: DemoFilter
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is LibraryEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val selectedFilter = uiState.data?.selectedFilter ?: initialFilter
    val previewMode = uiState.data?.previewMode ?: LibraryPreviewMode.READY
    val detailSection = uiState.data?.detailSection ?: DetailSection.OVERVIEW

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sample_library_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(LibraryEvent.ReloadRequested) }) {
                        Text(text = stringResource(R.string.sample_library_action_reload))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LibraryIntroCard()

            LibraryFilterSection(
                selectedFilter = selectedFilter,
                onFilterSelected = { viewModel.onEvent(LibraryEvent.FilterSelected(it)) }
            )

            LibraryPreviewSection(
                previewMode = previewMode,
                onPreviewModeSelected = { viewModel.onEvent(LibraryEvent.PreviewModeSelected(it)) }
            )

            LibraryDetailSectionSelector(
                detailSection = detailSection,
                onSectionSelected = { viewModel.onEvent(LibraryEvent.DetailSectionSelected(it)) }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AppBaseScreen(
                    uiState = uiState,
                    errorDialogConfig = ErrorDialogConfig(
                        onRetry = { viewModel.onEvent(LibraryEvent.ReloadRequested) }
                    ),
                    emptyContent = { EmptyLibraryState() },
                    onErrorDialogDismiss = viewModel::dismissErrorPopup
                ) { model ->
                    LibraryCatalogContent(
                        albums = model.albums,
                        onAlbumSelected = { viewModel.onEvent(LibraryEvent.AlbumSelected(it)) }
                    )
                }
            }
        }
    }
}
