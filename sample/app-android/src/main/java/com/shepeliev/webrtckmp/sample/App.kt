package com.shepeliev.webrtckmp.sample

import androidx.compose.animation.Crossfade
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.shepeliev.webrtckmp.sample.shared.Room

@Composable
fun App(room: Room) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebRTC KMP") }
            )
        }
    ) {

        val roomModel by room.model.subscribeAsState()

        Crossfade(targetState = roomModel) { model ->
            when (model.localStream) {
                null -> OpenMicrophoneAndCameraScreen(room)
                else -> VideoScreen(room)
            }
        }
    }
}
