import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.WebRtc
import kotlinx.coroutines.launch

@Composable
actual fun StartButton(
    setLocalStream: (MediaStream?) -> Unit,
    modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    var showDeviceSelect by remember { mutableStateOf(false) }

    // TODO permissions

    if (showDeviceSelect) {
        SelectDevicesDialog(
            onSelected = { camera, microphone, speaker ->
                scope.launch {
                    val stream = MediaDevices.getUserMedia {
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

                        speaker?.let {
                            WebRtc.setAudioOutputDevice(it)
                        }
                    }
                    setLocalStream(stream)
                    showDeviceSelect = false
                }
            },
            onDismiss = {
                showDeviceSelect = false
            }
        )
    }

    Button(
        onClick = {
            showDeviceSelect = true
        }
    ) {
        Text("Start")
    }
}
