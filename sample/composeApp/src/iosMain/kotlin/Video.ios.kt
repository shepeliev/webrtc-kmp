import WebRTC.RTCMTLVideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.VideoStreamTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun Video(videoTrack: VideoStreamTrack, modifier: Modifier, audioTrack: AudioStreamTrack?) {
    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                videoContentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                videoTrack.addRenderer(this)
            }
        },
        modifier = modifier,
        onRelease = { videoTrack.removeRenderer(it) }
    )
}
