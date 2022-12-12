package com.shepeliev.webrtckmp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.shepeliev.webrtckmp.sample.shared.RoomComponent
import dev.onvoid.webrtc.logging.Logging
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import java.awt.event.WindowEvent
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.system.exitProcess


@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    var lastError: Throwable? by mutableStateOf(null)

    WebRtc.configureBuilder {
        loggingSeverity = Logging.Severity.INFO
    }

    application(exitProcessOnExit = false) {
        System.setProperty("compose.interop.blending", "true")

        val lifecycle = LifecycleRegistry()

        val room = RoomComponent(
            componentContext = DefaultComponentContext(lifecycle),
            scope = CoroutineScope(EmptyCoroutineContext + CoroutineName("App")),
        )

        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
                WindowExceptionHandler {
                    lastError = it
                    window.dispatchEvent(WindowEvent(window, WindowEvent.WINDOW_CLOSING))
                }
            }
        ) {
            Window(onCloseRequest = ::exitApplication) {
                App(room)
            }
        }
    }

    if (lastError != null) {
        lastError?.printStackTrace()
        singleWindowApplication(
            state = WindowState(width = 200.dp, height = Dp.Unspecified),
            exitProcessOnExit = false
        ) {
            Text(lastError?.message ?: "Unknown error", Modifier.padding(8.dp))
        }

        exitProcess(1)
    } else {
        exitProcess(0)
    }
}
