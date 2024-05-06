import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import com.shepeliev.webrtckmp.AudioStreamTrack
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.VideoStreamTrack
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.MediaProvider

@Composable
actual fun Video(videoTrack: VideoStreamTrack, modifier: Modifier, audioTrack: AudioStreamTrack?) {
    val stream = remember { MediaStream() }

    val videoElement = remember {
        (document.createElement("video") as HTMLVideoElement).apply {
            srcObject = stream.js as MediaProvider
            autoplay = true
            style.position = "absolute"
        }
    }

    DisposableEffect(videoElement, stream) {
        document.body?.appendChild(videoElement)
        onDispose {
            document.body?.removeChild(videoElement)
            videoElement.srcObject = null
            stream.removeTrack(videoTrack)
            audioTrack?.let { stream.removeTrack(it) }
            stream.release()
        }
    }

    DisposableEffect(videoTrack) {
        stream.addTrack(videoTrack)
        onDispose { stream.removeTrack(videoTrack) }
    }

    DisposableEffect(audioTrack) {
        audioTrack?.let { stream.addTrack(it) }
        onDispose { audioTrack?.let { stream.removeTrack(it) } }
    }

    val density = LocalDensity.current

    Box(modifier = modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            with(density) {
                with(videoElement.style) {
                    top = "${coordinates.positionInWindow().y.toDp().value}px"
                    left = "${coordinates.positionInWindow().x.toDp().value}px"
                    width = "${coordinates.size.width.toDp().value}px"
                    height = "${coordinates.size.height.toDp().value}px"
                }
            }
        })
}
