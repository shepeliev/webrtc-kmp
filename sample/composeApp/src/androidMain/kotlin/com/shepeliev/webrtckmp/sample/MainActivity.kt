package com.shepeliev.webrtckmp.sample

import App
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.ApplicationContextProvider
import org.webrtc.Loggable
import org.webrtc.Logging
import org.webrtc.audio.JavaAudioDeviceModule

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val useHardwareAudioProcessing = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val audioModule =
            JavaAudioDeviceModule.builder(ApplicationContextProvider.getApplicationContext())
                .setUseHardwareAcousticEchoCanceler(useHardwareAudioProcessing)
                .setUseHardwareNoiseSuppressor(useHardwareAudioProcessing)
                .setSamplesReadyCallback { samples ->
                    // Here you can process the audio buffer as needed
                    println("onWebRtcAudioRecordSamplesReady: $samples")
                }
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .createAudioDeviceModule()

        val initializationOptionsBuilder = WebRtc.createInitializationOptionsBuilder()
            .setInjectableLogger(WebRtcLogger, Logging.Severity.LS_ERROR)
        val peerConnectionFactoryBuilder =
            WebRtc.createPeerConnectionFactoryBuilder(initializationOptionsBuilder)
                .setAudioDeviceModule(audioModule)

        WebRtc.configure(peerConnectionFactoryBuilder = peerConnectionFactoryBuilder)

        setContent {
            App()
        }
    }
}

private object WebRtcLogger : Loggable {
    override fun onLogMessage(message: String, severity: Logging.Severity, tag: String) {
        when (severity) {
            Logging.Severity.LS_ERROR -> Log.e(tag, message)
            Logging.Severity.LS_WARNING -> Log.w(tag, message)
            Logging.Severity.LS_INFO -> Log.i(tag, message)
            Logging.Severity.LS_VERBOSE -> Log.v(tag, message)
            Logging.Severity.LS_NONE -> {}
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
