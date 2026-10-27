import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.shepeliev.webrtckmp.AudioTrack
import com.shepeliev.webrtckmp.VideoTrack
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIViewContentMode
import swiftPMImport.com.shepeliev.webrtc.kmp.RTCMTLVideoView

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
