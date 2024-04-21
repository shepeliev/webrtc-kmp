import WebRTC.RTCAudioSession
import WebRTC.RTCAudioSessionConfiguration
import WebRTC.setConfiguration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

@Suppress("unused", "FunctionName")
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun MainViewController() = ComposeUIViewController {
    LaunchedEffect(Unit) {
        RTCAudioSessionConfiguration.initialize()
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            with(RTCAudioSession.sharedInstance()) {
                lockForConfiguration()
                useManualAudio = false
                setConfiguration(RTCAudioSessionConfiguration.webRTCConfiguration(), error.ptr)
                error.value?.let {
                    Logger.e { "Error setting WebRTC audio session configuration: ${it.localizedDescription}" }
                }
                unlockForConfiguration()
            }
        }
    }

    App()
}
