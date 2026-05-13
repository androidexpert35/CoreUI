package com.tony.coreui.components.basescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tony.coreui.R

/**
 * Generic screen for fatal errors or empty fallback flows.
 */
@Composable
fun ErrorScreen(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    primaryButtonText: String? = null,
    onPrimaryButtonClick: (() -> Unit)? = null,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null,
    tertiaryButtonText: String? = null,
    onTertiaryButtonClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .height(72.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = stringResource(R.string.coreui_error_icon_content_description),
                            modifier = Modifier.height(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!primaryButtonText.isNullOrEmpty() && onPrimaryButtonClick != null) {
                        FilledTonalButton(
                            onClick = onPrimaryButtonClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = primaryButtonText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!secondaryButtonText.isNullOrEmpty() && onSecondaryButtonClick != null) {
                        OutlinedButton(
                            onClick = onSecondaryButtonClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = secondaryButtonText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (!tertiaryButtonText.isNullOrEmpty() && onTertiaryButtonClick != null) {
                        OutlinedButton(
                            onClick = onTertiaryButtonClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = tertiaryButtonText)
                        }
                    }
                }
            }
        }
    }
}
