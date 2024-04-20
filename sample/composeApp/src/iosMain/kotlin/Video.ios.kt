import WebRTC.RTCMTLVideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.shepeliev.webrtckmp.VideoStreamTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun Video(track: VideoStreamTrack, modifier: Modifier) {
    UIKitView(
        factory = {
            RTCMTLVideoView().apply {
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                track.addRenderer(this)
            }
        },
        modifier = modifier,
    )
}
