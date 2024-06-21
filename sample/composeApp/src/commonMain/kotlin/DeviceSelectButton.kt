import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.MediaStream

@Composable
expect fun DeviceSelectButton(
    modifier: Modifier = Modifier,
    localStream: MediaStream,
)