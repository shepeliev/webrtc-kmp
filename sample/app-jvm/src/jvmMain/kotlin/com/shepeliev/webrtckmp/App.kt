package com.shepeliev.webrtckmp

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.shepeliev.webrtckmp.sample.shared.Room

@Composable
fun App(room: Room) {
    val roomModel by room.model.subscribeAsState()

    Crossfade(targetState = roomModel) { model ->
        when (model.localStream) {
            null -> SelectMicrophoneCameraScreen(room)
            else -> VideoScreen(room)
        }
    }
}