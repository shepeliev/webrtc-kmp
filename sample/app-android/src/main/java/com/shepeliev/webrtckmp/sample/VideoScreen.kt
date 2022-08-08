package com.shepeliev.webrtckmp.sample

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetpack.subscribeAsState
import com.shepeliev.webrtckmp.sample.shared.Room
import com.shepeliev.webrtckmp.videoTracks
import org.webrtc.RendererCommon

@Composable
fun VideoScreen(room: Room) {
    val roomModel by room.model.subscribeAsState()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(modifier = Modifier.fillMaxSize()) {
            val remoteStream = roomModel.remoteStream

            val animatedWeight by animateFloatAsState(
                targetValue = remoteStream?.let { 1f } ?: 0.01f
            )

            remoteStream?.let {
                Video(
                    track = it.videoTracks.first(),
                    modifier = Modifier.weight(animatedWeight),
                    scalingTypeMatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                    scalingTypeMismatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                )
            }

            roomModel.localStream?.let {
                Video(
                    track = it.videoTracks.first(),
                    modifier = Modifier.weight(1f),
                    scalingTypeMatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                    scalingTypeMismatchOrientation = RendererCommon.ScalingType.SCALE_ASPECT_FILL,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Crossfade(targetState = roomModel) {
                when {
                    it.isJoining -> CircularProgressIndicator()

                    it.roomId != null -> {
                        val roomId = roomModel.roomId
                        val context = LocalContext.current
                        val clipboardManager = LocalClipboardManager.current

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Room ID: $roomId", color = Color.White)

                            IconButton(onClick = {
                                val text = buildAnnotatedString { append(roomId!!) }
                                clipboardManager.setText(text)
                                Toast.makeText(context, "Room ID is copied.", Toast.LENGTH_SHORT)
                                    .show()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_content_copy),
                                    contentDescription = "Copy room ID",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }

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
