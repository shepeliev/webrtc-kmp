import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.VideoStreamTrack

@Composable
expect fun Video(videoTrack: VideoStreamTrack, modifier: Modifier = Modifier, audioTrack: AudioStreamTrack? = null)
