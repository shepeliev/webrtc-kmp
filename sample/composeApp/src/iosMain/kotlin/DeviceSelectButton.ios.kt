import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.videoTracks
import kotlinx.coroutines.launch

@Composable
actual fun DeviceSelectButton(
    modifier: Modifier,
    localStream: MediaStream
) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch { localStream.videoTracks.firstOrNull()?.switchCamera() }
        },
        modifier = modifier,
    ) {
        Text("Switch Camera")
    }
}