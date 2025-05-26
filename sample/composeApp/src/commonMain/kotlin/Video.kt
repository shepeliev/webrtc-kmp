import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.AudioTrack
import com.shepeliev.webrtckmp.VideoTrack

@Composable
expect fun Video(
    videoTrack: VideoTrack,
    modifier: Modifier = Modifier,
    audioTrack: AudioTrack? = null,
)
