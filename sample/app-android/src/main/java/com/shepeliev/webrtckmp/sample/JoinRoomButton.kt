package com.shepeliev.webrtckmp.sample

import androidx.compose.material.*
import androidx.compose.runtime.*

@Composable
fun JoinRoomButton(onJoin: (String) -> Unit, enabled: Boolean) {
    var isJoinDialogVisible by remember { mutableStateOf(false) }

    if (isJoinDialogVisible) {
        var roomId by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { isJoinDialogVisible = false },

            confirmButton = {
                TextButton(
                    onClick = {
                        onJoin(roomId)
                        isJoinDialogVisible = false
                    },
                    enabled = roomId.isNotBlank()
                ) {
                    Text("Join")
                }
            },

            dismissButton = {
                TextButton(onClick = { isJoinDialogVisible = false }) {
                    Text("Cancel")
                }
            },

            title = { Text("Join into room") },

            text = {
                OutlinedTextField(
                    value = roomId,
                    onValueChange = { roomId = it },
                    placeholder = { Text("Room ID") }
                )
            }
        )
    }

    Button(onClick = { isJoinDialogVisible = true }, enabled = enabled) {
        Text("Join")
    }
}
