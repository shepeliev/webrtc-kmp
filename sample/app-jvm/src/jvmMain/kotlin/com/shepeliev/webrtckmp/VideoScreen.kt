package com.shepeliev.webrtckmp

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.shepeliev.webrtckmp.sample.shared.Room

@Composable
fun VideoScreen(room: Room) {
    val roomModel by room.model.subscribeAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WebRTC") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            room.hangup()
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val remoteStream = roomModel.remoteStream

                val animatedWeight by animateFloatAsState(
                    targetValue = remoteStream?.let { 1f } ?: 0.01f
                )

                remoteStream?.let {
                    it.videoTracks.firstOrNull()?.let { track ->
                        Video(
                            track = track,
                            modifier = Modifier.weight(animatedWeight),
                        )
                    }
                }

                roomModel.localStream?.let {
                    it.videoTracks.firstOrNull()?.let { track ->
                        Video(
                            track = track,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Crossfade(targetState = roomModel) {
                    when {
                        it.isJoining -> CircularProgressIndicator()

                        it.roomId != null -> {
                            val roomId = roomModel.roomId
                            val clipboardManager = LocalClipboardManager.current

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Room ID: $roomId", color = Color.White)

                                IconButton(onClick = {
                                    val text = buildAnnotatedString { append(roomId!!) }
                                    clipboardManager.setText(text)
                                }) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (roomModel.roomId == null) {
                            Button(onClick = room::createRoom, enabled = !roomModel.isJoining) {
                                Text("Create")
                            }

                            JoinRoomButton(onJoin = room::joinRoom, enabled = !roomModel.isJoining)
                        }

                        Button(onClick = room::hangup) {
                            Text("Hangup")
                        }
                    }
                }
            }
        }
    }
}