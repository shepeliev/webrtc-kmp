import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import com.shepeliev.webrtckmp.MediaDeviceInfo
import com.shepeliev.webrtckmp.MediaDeviceKind
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.WebRtc

@Composable
internal fun SelectDevicesDialog(
    modifier: Modifier = Modifier,
    onSelected: (camera: MediaDeviceInfo?, microphone: MediaDeviceInfo?, speaker: MediaDeviceInfo?) -> Unit,
    onDismiss: () -> Unit,
) {
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

        if (camera == null || !cameraList.any { it.deviceId == camera?.deviceId }) {
            camera = cameraList.firstOrNull()
        }

        if (microphone == null || !microphoneList.any { it.deviceId == microphone?.deviceId }) {
            microphone = microphoneList.firstOrNull()
        }

        if (speaker == null || !speakerList.any { it.deviceId == speaker?.deviceId }) {
            speaker = WebRtc.getDefaultAudioOutput()
        }
    }

    LaunchedEffect(Unit) {
        refreshDevices()
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface{
            Column(
                modifier = modifier,
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
                    ) { device ->
                        if (device != null) {
                            microphone = device
                            WebRtc.setAudioInputDevice(device)
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Select Speaker")
                    DeviceSelector(
                        current = speaker,
                        list = speakerList,
                    ) { device ->
                        if (device != null) {
                            speaker = device
                            WebRtc.setAudioOutputDevice(device)
                        }
                    }
                }

                Button(
                    onClick = {
                        onSelected(
                            camera,
                            microphone,
                            speaker,
                        )
                    }
                ) {
                    Text("Select")
                }

                TextButton(
                    onClick = onDismiss,
                ) {
                    Text("Cancel")
                }
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
            if (allowSelectNone) {
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