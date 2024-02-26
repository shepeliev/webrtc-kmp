package com.shepeliev.webrtckmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.sample.shared.Room
import kotlinx.coroutines.launch

@Composable
fun SelectMicrophoneCameraScreen(room: Room) {
    val scope = rememberCoroutineScope()

    var camera: MediaDeviceInfo? by remember { mutableStateOf(null) }
    var microphone: MediaDeviceInfo? by remember { mutableStateOf(null) }
    var speaker: MediaDeviceInfo? by remember { mutableStateOf(WebRtc.getDefaultAudioOutput()) }

    var cameraList: Set<MediaDeviceInfo> by remember { mutableStateOf(emptySet()) }
    var microphoneList: Set<MediaDeviceInfo> by remember { mutableStateOf(emptySet()) }
    var speakerList: Set<MediaDeviceInfo> by remember { mutableStateOf(emptySet()) }

    suspend fun refreshDevices() {
        val devices = MediaDevices.enumerateDevices()
        cameraList = devices.filter { it.kind == MediaDeviceKind.VideoInput }.toSet()
        microphoneList = devices.filter { it.kind == MediaDeviceKind.AudioInput }.toSet()
        speakerList = devices.filter { it.kind == MediaDeviceKind.AudioOutput }.toSet()

        if(camera == null || !cameraList.any { it.deviceId == camera?.deviceId }) {
            camera = cameraList.firstOrNull()
        }

        if(microphone == null || !microphoneList.any { it.deviceId == microphone?.deviceId }) {
            microphone = microphoneList.firstOrNull()
        }

        if(speaker == null || !speakerList.any { it.deviceId == speaker?.deviceId }) {
            speaker = WebRtc.getDefaultAudioOutput()
        }
    }

    val deviceObserver = object: MediaDeviceListener {
        override fun deviceConnected(device: MediaDeviceInfo) {
            when(device.kind) {
                MediaDeviceKind.VideoInput -> cameraList = cameraList + device
                MediaDeviceKind.AudioInput -> microphoneList = microphoneList + device
                MediaDeviceKind.AudioOutput -> speakerList = speakerList + device
            }
        }

        override fun deviceDisconnected(device: MediaDeviceInfo) {
            when(device.kind) {
                MediaDeviceKind.VideoInput -> {
                    cameraList = cameraList - device
                    if(camera == device) {
                        camera = null
                    }
                }
                MediaDeviceKind.AudioInput -> {
                    microphoneList = microphoneList - device
                    if(microphone == device) {
                        microphone = null
                    }
                }
                MediaDeviceKind.AudioOutput -> {
                    speakerList = speakerList - device
                    if(speaker == device) {
                        speaker = WebRtc.getDefaultAudioOutput()
                    }
                }
            }
        }
    }

    DisposableEffect(room) {
        WebRtc.addDeviceChangeListener(deviceObserver)
        onDispose {
            WebRtc.removeDeviceChangeListener(deviceObserver)
        }
    }

    LaunchedEffect(room) {
        refreshDevices()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Devices") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                refreshDevices()
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Select Camera")
                DeviceSelector(
                    current = camera,
                    list = cameraList,
                ) {
                    camera = it
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Select Microphone")
                DeviceSelector(
                    current = microphone,
                    list = microphoneList,
                ) {
                    microphone = it
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Select Speaker")
                DeviceSelector(
                    current = speaker,
                    list = speakerList,
                ) {
                    if(it != null) {
                        speaker = it
                        WebRtc.setAudioOutputDevice(it)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    room.openUserMedia {
                        camera?.let {
                            video {
                                deviceId(it.deviceId)
                            }
                        }

                        microphone?.let {
                            audio {
                                deviceId(it.deviceId)
                            }
                        }
                    }
                },
                enabled = camera != null || microphone != null
            ) {
                Text("Confirm")
            }

            OutlinedButton(
                onClick = {
                    room.openDesktopMedia()
                },
            ) {
                Text("Share Desktop")
            }
        }
    }
}

@Composable
private fun DeviceSelector(
    modifier: Modifier = Modifier,
    list: Set<MediaDeviceInfo>?,
    current: MediaDeviceInfo?,
    allowSelectNone: Boolean = false,
    onSelected: (MediaDeviceInfo?) -> Unit,
) {
    var isOpen by remember { mutableStateOf(false) }
    list?.let { devices ->
        TextButton(
            modifier = modifier,
            onClick = {
                isOpen = true
            }
        ) {
            Text(current?.label ?: "None")
        }
        DropdownMenu(
            expanded = isOpen,
            onDismissRequest = { isOpen = false }) {
            if(allowSelectNone) {
                DropdownMenuItem(onClick = {
                    onSelected(null)
                    isOpen = false
                }) {
                    Text(text = "None")
                }
            }

            devices.forEach { device ->
                DropdownMenuItem(onClick = {
                    onSelected(device)
                    isOpen = false
                }) {
                    Text(text = device.label)
                }
            }
        }
    } ?: CircularProgressIndicator()
}