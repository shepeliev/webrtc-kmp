import WebRTC.RTCAudioBuffer
import WebRTC.RTCAudioCustomProcessingDelegateProtocol
import WebRTC.RTCAudioSession
import WebRTC.RTCAudioSessionConfiguration
import WebRTC.RTCDefaultAudioProcessingModule
import WebRTC.RTCDefaultVideoDecoderFactory
import WebRTC.RTCDefaultVideoEncoderFactory
import WebRTC.RTCPeerConnectionFactory
import WebRTC.RTCVideoEncoderFactorySimulcast
import WebRTC.setConfiguration
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import com.shepeliev.webrtckmp.WebRtc
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError
import platform.darwin.NSObject
import platform.posix.size_t

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

    val encoderFactory = RTCDefaultVideoEncoderFactory()
    val peerConnectionFactory =
        RTCPeerConnectionFactory(
            bypassVoiceProcessing = false,
            RTCVideoEncoderFactorySimulcast(encoderFactory, encoderFactory),
            RTCDefaultVideoDecoderFactory(),
            audioProcessingModule = RTCDefaultAudioProcessingModule(
                config = null,
                capturePostProcessingDelegate = AudioProcessingDelegate(),
                renderPreProcessingDelegate = null
            )
        )

    WebRtc.configure(rtcPeerConnectionFactory = peerConnectionFactory)

    App()
}

@OptIn(ExperimentalForeignApi::class)
internal class AudioProcessingDelegate : NSObject(), RTCAudioCustomProcessingDelegateProtocol {
    override fun audioProcessingInitializeWithSampleRate(sampleRateHz: size_t, channels: size_t) {
        println("Audio processing initialized with sample rate: $sampleRateHz Hz, channels: $channels")
    }

    override fun audioProcessingProcess(audioBuffer: RTCAudioBuffer) {
        println("Audio processing in progress with buffer: $audioBuffer")
        // Here you can process the audio buffer as needed
    }

    override fun audioProcessingRelease() {
        println("Audio processing released")
    }
}
