package com.shepeliev.webrtckmp.sample

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shepeliev.webrtckmp.sample.shared.Room

@Composable
fun OpenMicrophoneAndCameraScreen(room: Room) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        OpenCameraAndMicrophoneButton(onClick = room::openUserMedia)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun OpenCameraAndMicrophoneButton(onClick: () -> Unit) {
    val permissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
        )
    )

    var isRationaleVisible by remember { mutableStateOf(false) }

    if (isRationaleVisible) {
        AlertDialog(
            text = { Text("Please grant camera and microphone permissions") },
            onDismissRequest = { isRationaleVisible = false },
            confirmButton = {
                val context = LocalContext.current
                TextButton(onClick = {
                    if (!permissions.shouldShowRationale) {
                        context.navigateToAppSettings()
                    } else {
                        permissions.launchMultiplePermissionRequest()
                    }
                    isRationaleVisible = false
                }) {
                    Text("Grant permissions")
                }
            }
        )
    }

    Button(onClick = {
        when {
            permissions.allPermissionsGranted -> onClick()

            else -> isRationaleVisible = true
        }
    }) {
        Text("Open camera and microphone")
    }
}
