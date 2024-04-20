import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.shepeliev.webrtckmp.VideoStreamTrack

@Composable
expect fun Video(track: VideoStreamTrack,  modifier: Modifier = Modifier)
