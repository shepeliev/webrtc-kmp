package com.shepeliev.webrtckmp.sample

import android.Manifest
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.shepeliev.webrtckmp.sample.shared.Room

@Composable
fun OpenMicrophoneAndCameraScreen(room: Room) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OpenCameraAndMicrophoneButton(onClick = room::openUserMedia)
            Button(onClick = { room.openUserMedia(videoDeviceId = "color-bars") }) {
                Text("Open test media stream")
            }
        }
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

    val context = LocalContext.current
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    if (isRationaleVisible) {
        AlertDialog(
            text = { Text("Please grant camera and microphone permissions") },
            onDismissRequest = { isRationaleVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    if (!permissions.shouldShowRationale && preferences.getBoolean("should_open_app_settings", false)) {
                        context.navigateToAppSettings()
                    } else {
                        permissions.launchMultiplePermissionRequest()
                        preferences.edit { putBoolean("should_open_app_settings", true) }
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
            permissions.allPermissionsGranted -> {
                preferences.edit { putBoolean("should_open_app_settings", false) }
                onClick()
            }

            else -> isRationaleVisible = true
        }
    }) {
        Text("Open camera and microphone")
    }
}
