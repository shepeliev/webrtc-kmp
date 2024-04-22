import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.MediaTrackConstraints
import com.shepeliev.webrtckmp.WebRtc
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.launch

@Composable
actual fun DeviceSelectButton(
    modifier: Modifier,
    localStream: MediaStream,
) {
    val scope = rememberCoroutineScope()
    var showDeviceSelect by remember { mutableStateOf(false) }

    if (showDeviceSelect) {
        SelectDevicesDialog(
            onSelected = { camera, microphone, speaker ->
                camera?.let {
                    scope.launch {
                        localStream.videoTracks.firstOrNull()?.switchCamera(
                            deviceId = it.deviceId,
                        )
                    }
                }
                microphone?.let {
                    WebRtc.setAudioInputDevice(it)
                }
                speaker?.let {
                    WebRtc.setAudioOutputDevice(it)
                }
                showDeviceSelect = false
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
        Text("Input/Output")
    }
}