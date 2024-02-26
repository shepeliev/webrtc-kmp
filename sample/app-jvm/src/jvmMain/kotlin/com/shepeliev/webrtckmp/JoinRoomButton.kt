package com.shepeliev.webrtckmp

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterialApi::class)
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
