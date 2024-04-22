import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.launch

@Composable
actual fun StartButton(setLocalStream: (MediaStream?) -> Unit, modifier: Modifier) {
    val scope = rememberCoroutineScope()

    val openMediaStreams = remember<() -> Unit> {
        {
            scope.launch {
                val stream = MediaDevices.getUserMedia(audio = true, video = true)
                setLocalStream(stream)
            }
        }
    }

    Button(
        onClick = openMediaStreams,
        modifier = modifier,
    ) {
        Text("Start")
    }
}
