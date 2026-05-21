package com.tony.coreui.sample.presentation.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tony.coreui.presentation.components.basescreen.AppBaseScreen
import com.tony.coreui.presentation.components.basescreen.BaseLoadingType
import com.tony.coreui.presentation.components.basescreen.BaseScreenRenderPolicy
import com.tony.coreui.presentation.state.UIError
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.sample.R
import com.tony.coreui.sample.app.SampleAppContainer
import com.tony.coreui.sample.app.sampleViewModelFactory
import com.tony.coreui.sample.domain.model.DetailSection
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DetailRoute(
    albumId: Long,
    initialSection: DetailSection,
    appContainer: SampleAppContainer
) {
    val viewModel: DetailViewModel = viewModel(
        factory = sampleViewModelFactory {
            DetailViewModel(
                albumId = albumId,
                repository = appContainer.showcaseRepository,
                navigationManager = appContainer.navigationManager,
                uiErrorMapper = appContainer.detailUiErrorMapper
            )
        }
    )

    DetailScreen(
        viewModel = viewModel,
        albumId = albumId,
        initialSection = initialSection
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailScreen(
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
                    Column {
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
                contentWithState = { model, state ->
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
            ) { model ->
                DetailContent(
                    model = model,
                    uiState = uiState,
                    initialSection = initialSection,
                    onRefresh = { viewModel.onEvent(DetailEvent.RefreshRequested) },
                    onServiceFailure = { viewModel.onEvent(DetailEvent.ServiceFailureRequested) },
                    onHostWarning = { viewModel.onEvent(DetailEvent.HostWarningRequested) },
                    onOpenRelated = { viewModel.onEvent(DetailEvent.RelatedAlbumRequested) },
                    onReset = { viewModel.onEvent(DetailEvent.ResetToDefaultsRequested) }
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    model: DetailUiModel,
    uiState: UIState<DetailUiModel>,
    initialSection: DetailSection,
    onRefresh: () -> Unit,
    onServiceFailure: () -> Unit,
    onHostWarning: () -> Unit,
    onOpenRelated: () -> Unit,
    onReset: () -> Unit
) {
    var selectedSection by rememberSaveable(model.id) { mutableStateOf(initialSection) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ElevatedCard(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = model.statsLine,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = model.summary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = model.lastUpdatedLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        val inlineError = uiState.error
        if (inlineError != null && inlineError.displayMode == UIErrorDisplayMode.NONE) {
            item {
                HostManagedWarning(error = inlineError)
            }
        }

        item {
            PrimaryTabRow(selectedTabIndex = selectedSection.ordinal) {
                DetailSection.entries.forEach { section ->
                    Tab(
                        selected = selectedSection == section,
                        onClick = { selectedSection = section },
                        text = { Text(text = section.label()) }
                    )
                }
            }
        }

        if (selectedSection == DetailSection.OVERVIEW) {
            item {
                DetailSectionCard(
                    title = stringResource(R.string.sample_detail_defaults_title),
                    body = stringResource(R.string.sample_detail_defaults_body),
                    bullets = model.defaultsHighlights
                )
            }
        } else {
            item {
                DetailSectionCard(
                    title = stringResource(R.string.sample_detail_customize_title),
                    body = stringResource(R.string.sample_detail_customize_body),
                    bullets = model.customizationHighlights
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onRefresh,
                    label = { Text(text = stringResource(R.string.sample_detail_action_refresh)) }
                )
                FilterChip(
                    selected = false,
                    onClick = onOpenRelated,
                    enabled = model.relatedAlbumId != null,
                    label = {
                        Text(
                            text = model.relatedAlbumTitle
                                ?: stringResource(R.string.sample_detail_action_related)
                        )
                    }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onServiceFailure,
                    label = { Text(text = stringResource(R.string.sample_detail_action_service_failure)) }
                )
                FilterChip(
                    selected = false,
                    onClick = onHostWarning,
                    label = { Text(text = stringResource(R.string.sample_detail_action_host_warning)) }
                )
            }
        }

        item {
            OutlinedButton(onClick = onReset) {
                Text(text = stringResource(R.string.sample_detail_action_reset))
            }
        }
    }
}

@Composable
private fun DetailSectionCard(
    title: String,
    body: String,
    bullets: List<String>
) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            bullets.forEach { bullet ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = bullet,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun HostManagedWarning(error: UIError) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sample_detail_inline_warning_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = stringResource(R.string.sample_detail_inline_warning_body),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun DetailServiceErrorScreen(
    error: UIError,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.sample_detail_service_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = stringResource(R.string.sample_detail_service_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onRetry) {
                        Text(text = stringResource(R.string.sample_detail_error_primary))
                    }
                    TextButton(onClick = onReset) {
                        Text(text = stringResource(R.string.sample_detail_error_secondary))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection.label(): String = when (this) {
    DetailSection.OVERVIEW -> stringResource(R.string.sample_detail_overview_tab)
    DetailSection.CUSTOMIZATION -> stringResource(R.string.sample_detail_customization_tab)
}
