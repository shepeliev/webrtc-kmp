package com.shepeliev.webrtckmp.sample

import App
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shepeliev.webrtckmp.WebRtc
import org.webrtc.Loggable
import org.webrtc.Logging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initializationOptionsBuilder = WebRtc.createInitializationOptionsBuilder()
            .setInjectableLogger(WebRtcLogger, Logging.Severity.LS_ERROR)
        val peerConnectionFactoryBuilder = WebRtc.createPeerConnectionFactoryBuilder(initializationOptionsBuilder = initializationOptionsBuilder)
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
