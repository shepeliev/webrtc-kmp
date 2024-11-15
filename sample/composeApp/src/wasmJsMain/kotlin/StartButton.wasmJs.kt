import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import kotlinx.coroutines.launch

@Composable
actual fun StartButton(setLocalStream: (MediaStream?) -> Unit, modifier: Modifier) {
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            scope.launch {
                val stream = MediaDevices.getUserMedia(audio = true, video = true)
                setLocalStream(stream)
            }
        },
        modifier = modifier,
    ) {
        Text("Start")
    }
}
