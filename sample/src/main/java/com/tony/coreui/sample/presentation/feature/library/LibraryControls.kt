package com.tony.coreui.sample.presentation.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tony.coreui.sample.R
import com.tony.coreui.sample.domain.model.DemoFilter
import com.tony.coreui.sample.domain.model.DetailSection
import com.tony.coreui.sample.domain.model.LibraryPreviewMode

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

/** Filter controls for the list showcase screen. */
@Composable
internal fun LibraryFilterSection(
    selectedFilter: DemoFilter,
    onFilterSelected: (DemoFilter) -> Unit
) {
    ControlSection(title = stringResource(R.string.sample_library_filter_label)) {
        DemoFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(text = filter.label()) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
    }
}

/** Preview mode controls for the list showcase screen. */
@Composable
internal fun LibraryPreviewSection(
    previewMode: LibraryPreviewMode,
    onPreviewModeSelected: (LibraryPreviewMode) -> Unit
) {
    ControlSection(title = stringResource(R.string.sample_library_preview_label)) {
        LibraryPreviewMode.entries.forEach { mode ->
            FilterChip(
                selected = previewMode == mode,
                onClick = { onPreviewModeSelected(mode) },
                label = { Text(text = mode.label()) }
            )
        }
    }
}

/** Controls the section opened by typed detail links in the sample. */
@Composable
internal fun LibraryDetailSectionSelector(
    detailSection: DetailSection,
    onSectionSelected: (DetailSection) -> Unit
) {
    ControlSection(title = stringResource(R.string.sample_library_detail_section_label)) {
        DetailSection.entries.forEach { section ->
            FilterChip(
                selected = detailSection == section,
                onClick = { onSectionSelected(section) },
                label = { Text(text = section.label()) }
            )
        }
    }
}

@Composable
internal fun DemoFilter.label(): String = when (this) {
    DemoFilter.ALL -> stringResource(R.string.sample_library_filter_all)
    DemoFilter.FOCUS_READY -> stringResource(R.string.sample_library_filter_focus)
    DemoFilter.DOWNLOADED -> stringResource(R.string.sample_library_filter_downloaded)
}

@Composable
internal fun LibraryPreviewMode.label(): String = when (this) {
    LibraryPreviewMode.READY -> stringResource(R.string.sample_library_preview_ready)
    LibraryPreviewMode.EMPTY -> stringResource(R.string.sample_library_preview_empty)
    LibraryPreviewMode.NETWORK_DIALOG -> stringResource(R.string.sample_library_preview_network)
    LibraryPreviewMode.VALIDATION_DIALOG -> stringResource(R.string.sample_library_preview_validation)
}

@Composable
internal fun DetailSection.label(): String = when (this) {
    DetailSection.OVERVIEW -> stringResource(R.string.sample_detail_overview_tab)
    DetailSection.CUSTOMIZATION -> stringResource(R.string.sample_detail_customization_tab)
}
