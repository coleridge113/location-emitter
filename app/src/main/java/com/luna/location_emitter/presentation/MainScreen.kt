package com.luna.location_emitter.presentation


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun MainScreen(
    modifier: Modifier,
    onStartEmitting: () -> Unit,
    onStopEmitting: () -> Unit
) {
    var enabled by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EnableButton(
            enabled = enabled,
            onToggle = {
                enabled = !enabled
                if (enabled) {
                    onStartEmitting()
                } else {
                    onStopEmitting()
                }
            }
        )

        Text(
            text = if (enabled) "Emitting is ON" else "Emitting is OFF",
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun EnableButton(
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Button(onClick = onToggle) {
        Text(if (enabled) "Disable" else "Enable")
    }
}
