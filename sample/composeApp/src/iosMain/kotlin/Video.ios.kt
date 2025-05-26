import WebRTC.RTCMTLVideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.AudioTrack
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun Video(
    videoTrack: VideoTrack,
    modifier: Modifier,
    audioTrack: AudioTrack?,
) {
    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                videoTrack.addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = { videoTrack.removeRenderer(it) },
    )
}
