package com.tony.coreui.presentation.components.basescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tony.coreui.R

/**
 * Displays a Material 3 loading surface for full-screen or overlay presentation.
 *
 * When [progress] is `null`, an indeterminate circular indicator is shown. When [progress] is
 * provided, the component switches to a linear determinate indicator and exposes the percentage in
 * the supporting text.
 *
 * @param modifier [Modifier] applied to the full-screen container.
 * @param loadingText optional message shown as the primary loading label. When `null`, a default
 * localized string is used.
 * @param progress optional progress percentage in the `0..100` range.
 * @param backgroundColor background color drawn behind the loading card.
 */
@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
    loadingText: String? = null,
    progress: Int? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
) {
    val resolvedLoadingText = loadingText ?: stringResource(R.string.coreui_loading_label)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (progress == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = resolvedLoadingText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )

                Text(
                    text = if (progress == null) {
                        stringResource(R.string.coreui_loading_secondary_label)
                    } else {
                        stringResource(R.string.coreui_loading_progress_label, progress)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
