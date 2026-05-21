package com.tony.coreui.sample.presentation.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tony.coreui.presentation.state.UIErrorDisplayMode
import com.tony.coreui.presentation.state.UIState
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DetailSection

/** Main detail body rendered by `AppBaseScreen.contentWithState`. */
@Composable
internal fun DetailContent(
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
            DetailHeaderCard(model = model)
        }

        val inlineError = uiState.error
        if (inlineError != null && inlineError.displayMode == UIErrorDisplayMode.NONE) {
            item {
                HostManagedWarning(error = inlineError)
            }
        }

        item {
            DetailSectionTabs(
                selectedSection = selectedSection,
                onSectionSelected = { selectedSection = it }
            )
        }

        item {
            if (selectedSection == DetailSection.OVERVIEW) {
                DetailSectionCard(
                    title = stringResource(R.string.sample_detail_defaults_title),
                    body = stringResource(R.string.sample_detail_defaults_body),
                    bullets = model.defaultsHighlights
                )
            } else {
                DetailSectionCard(
                    title = stringResource(R.string.sample_detail_customize_title),
                    body = stringResource(R.string.sample_detail_customize_body),
                    bullets = model.customizationHighlights
                )
            }
        }

        item {
            DetailPrimaryActions(
                relatedAlbumTitle = model.relatedAlbumTitle,
                relatedAlbumEnabled = model.relatedAlbumId != null,
                onRefresh = onRefresh,
                onOpenRelated = onOpenRelated
            )
        }

        item {
            DetailSecondaryActions(
                onServiceFailure = onServiceFailure,
                onHostWarning = onHostWarning
            )
        }

        item {
            OutlinedButton(onClick = onReset) {
                Text(text = stringResource(R.string.sample_detail_action_reset))
            }
        }
    }
}

/** Hero card summarizing the currently opened album. */
@Composable
private fun DetailHeaderCard(model: DetailUiModel) {
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

/** Section tabs used to switch between defaults and customization narratives. */
@Composable
private fun DetailSectionTabs(
    selectedSection: DetailSection,
    onSectionSelected: (DetailSection) -> Unit
) {
    PrimaryTabRow(selectedTabIndex = selectedSection.ordinal) {
        DetailSection.entries.forEach { section ->
            Tab(
                selected = selectedSection == section,
                onClick = { onSectionSelected(section) },
                text = { Text(text = section.label()) }
            )
        }
    }
}

/** First row of actions that cover refresh and typed related-album navigation. */
@Composable
private fun DetailPrimaryActions(
    relatedAlbumTitle: String?,
    relatedAlbumEnabled: Boolean,
    onRefresh: () -> Unit,
    onOpenRelated: () -> Unit
) {
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
            enabled = relatedAlbumEnabled,
            label = {
                Text(
                    text = relatedAlbumTitle
                        ?: stringResource(R.string.sample_detail_action_related)
                )
            }
        )
    }
}

/** Second row of actions that demonstrate custom error handling paths. */
@Composable
private fun DetailSecondaryActions(
    onServiceFailure: () -> Unit,
    onHostWarning: () -> Unit
) {
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
