package com.tony.coreui.sample.presentation.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tony.coreui.presentation.components.basescreen.AppBaseScreen
import com.tony.coreui.presentation.components.basescreen.ErrorDialogConfig
import com.tony.coreui.sample.R
import com.tony.coreui.sample.app.SampleAppContainer
import com.tony.coreui.sample.app.sampleViewModelFactory
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode
import kotlinx.coroutines.flow.collectLatest

/**
 * Route entry point for the defaults-first sample feature.
 *
 * The navigation host passes the typed initial filter argument here and the route constructs the
 * screen's ViewModel from the shared sample container.
 */
@Composable
fun LibraryRoute(
    initialFilter: DemoFilter,
    appContainer: SampleAppContainer
) {
    val viewModel: LibraryViewModel = viewModel(
        factory = sampleViewModelFactory {
            LibraryViewModel(
                initialFilter = initialFilter,
                repository = appContainer.showcaseRepository,
                navigationManager = appContainer.navigationManager
            )
        }
    )

    LibraryScreen(
        viewModel = viewModel,
        initialFilter = initialFilter
    )
}

/**
 * Demonstrates `AppBaseScreen` in its most convenient form.
 *
 * This screen intentionally sticks close to the defaults so consumers can see how little feature
 * code is required when they accept the library's built-in loading, dialog-error, and empty-state
 * behavior.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LibraryScreen(
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
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(text = stringResource(R.string.sample_library_defaults_title)) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = stringResource(R.string.sample_library_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.sample_library_defaults_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            ControlSection(
                title = stringResource(R.string.sample_library_filter_label)
            ) {
                DemoFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { viewModel.onEvent(LibraryEvent.FilterSelected(filter)) },
                        label = { Text(text = filter.label()) },
                        colors = FilterChipDefaults.filterChipColors()
                    )
                }
            }

            ControlSection(
                title = stringResource(R.string.sample_library_preview_label)
            ) {
                LibraryPreviewMode.entries.forEach { mode ->
                    FilterChip(
                        selected = previewMode == mode,
                        onClick = { viewModel.onEvent(LibraryEvent.PreviewModeSelected(mode)) },
                        label = { Text(text = mode.label()) }
                    )
                }
            }

            ControlSection(
                title = stringResource(R.string.sample_library_detail_section_label)
            ) {
                DetailSection.entries.forEach { section ->
                    FilterChip(
                        selected = detailSection == section,
                        onClick = { viewModel.onEvent(LibraryEvent.DetailSectionSelected(section)) },
                        label = { Text(text = section.label()) }
                    )
                }
            }

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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = model.albums,
                            key = LibraryAlbumCardUiModel::id
                        ) { album ->
                            LibraryAlbumCard(
                                album = album,
                                onClick = {
                                    viewModel.onEvent(LibraryEvent.AlbumSelected(album.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Groups a set of related controls under a small section label. */
@Composable
private fun ControlSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

/** Compact album card used by the list screen's `AppBaseScreen` content lambda. */
@Composable
private fun LibraryAlbumCard(
    album: LibraryAlbumCardUiModel,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = album.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = album.badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = album.summary,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = album.meta,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                )
                Text(
                    text = stringResource(R.string.sample_library_open_album),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/** Empty-state body delegated to `AppBaseScreen.emptyContent`. */
@Composable
private fun EmptyLibraryState() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sample_library_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.sample_library_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DemoFilter.label(): String = when (this) {
    DemoFilter.ALL -> stringResource(R.string.sample_library_filter_all)
    DemoFilter.FOCUS_READY -> stringResource(R.string.sample_library_filter_focus)
    DemoFilter.DOWNLOADED -> stringResource(R.string.sample_library_filter_downloaded)
}

@Composable
private fun LibraryPreviewMode.label(): String = when (this) {
    LibraryPreviewMode.READY -> stringResource(R.string.sample_library_preview_ready)
    LibraryPreviewMode.EMPTY -> stringResource(R.string.sample_library_preview_empty)
    LibraryPreviewMode.NETWORK_DIALOG -> stringResource(R.string.sample_library_preview_network)
    LibraryPreviewMode.VALIDATION_DIALOG -> stringResource(R.string.sample_library_preview_validation)
}

@Composable
private fun DetailSection.label(): String = when (this) {
    DetailSection.OVERVIEW -> stringResource(R.string.sample_detail_overview_tab)
    DetailSection.CUSTOMIZATION -> stringResource(R.string.sample_detail_customization_tab)
}
