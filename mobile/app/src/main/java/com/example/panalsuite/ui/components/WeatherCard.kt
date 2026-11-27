package com.example.panalsuite.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.panalsuite.R
import com.example.panalsuite.viewmodel.WeatherUiState
import com.example.panalsuite.viewmodel.WeatherViewModel
import kotlin.math.roundToInt

// Muestra el clima actual de Santiago usando la API externa Open-Meteo
@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.weather_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Crossfade(targetState = state, label = "weather") { current ->
                when (current) {
                    is WeatherUiState.Loading -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = stringResource(R.string.weather_loading),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    is WeatherUiState.Success -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = weatherIcon(current.info.weatherCode),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.weather_temperature, current.info.temperature.roundToInt()),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.weather_description,
                                    current.info.description,
                                    current.info.windSpeed.roundToInt()
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is WeatherUiState.Error -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = current.message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = viewModel::load) {
                            Text(text = stringResource(R.string.weather_retry))
                        }
                    }
                }
            }

            // Open-Meteo exige mostrar la atribucion con un enlace (licencia CC BY 4.0)
            Text(
                text = stringResource(R.string.weather_attribution),
                modifier = Modifier.clickable { uriHandler.openUri("https://open-meteo.com/") },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun weatherIcon(code: Int): ImageVector = when (code) {
    0, 1 -> Icons.Rounded.WbSunny
    in 51..67, in 80..99 -> Icons.Rounded.Umbrella
    else -> Icons.Rounded.Cloud
}
