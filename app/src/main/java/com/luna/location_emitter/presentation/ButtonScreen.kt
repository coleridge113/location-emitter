package com.luna.location_emitter.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.luna.location_emitter.utils.RouteEmitter
import com.luna.location_emitter.utils.radar.RadarTrip
import org.koin.compose.koinInject

@Composable
fun ButtonScreen(
    modifier: Modifier,
    routeEmitter: RouteEmitter
) {
    var enabled by remember { mutableStateOf(false) }
    var externalId by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    
    LaunchedEffect(enabled) {
        routeEmitter.apply {
            if (enabled) start() else stop()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DataInput()
        EnableButton(
            enabled = enabled,
            onToggle = { enabled = !enabled }
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

@Composable
fun DataInput() {
    var userId by remember { mutableStateOf("metromart-user") }
    var externalId by remember { mutableStateOf("") }

    var userIdError by remember { mutableStateOf<String?>(null) }
    var externalIdError by remember { mutableStateOf<String?>(null) }

    val isValid = userId.isNotBlank() && externalId.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = userId,
            onValueChange = {
                userId = it
                userIdError = if (it.isBlank()) "User ID cannot be empty" else null
            },
            label = { Text("User ID") },
            isError = userIdError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (userIdError != null) {
            Text(
                text = userIdError!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = externalId,
            onValueChange = {
                externalId = it
                externalIdError = if (it.isBlank()) "External ID cannot be empty" else null
            },
            label = { Text("External ID") },
            isError = externalIdError != null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (externalIdError != null) {
            Text(
                text = externalIdError!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                userIdError = if (userId.isBlank()) "User ID cannot be empty" else null
                externalIdError = if (externalId.isBlank()) "External ID cannot be empty" else null

                if (isValid) {
                    RadarTrip.setTripData(
                        input = externalId,
                        userId = userId
                    )
                }
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Set Data")
        }
    }
}
