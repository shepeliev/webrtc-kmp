package com.shepeliev.apprtckmm.shared.call

import com.arkivanov.mvikotlin.core.store.Store
import com.shepeliev.apprtckmm.shared.call.CallStore.Intent
import com.shepeliev.apprtckmm.shared.call.CallStore.Label
import com.shepeliev.apprtckmm.shared.call.CallStore.State
import com.shepeliev.webrtckmm.UserMedia
import com.shepeliev.webrtckmm.VideoTrack

internal interface CallStore : Store<Intent, State, Label> {
    sealed class Intent {
        object Disconnect : Intent()
        object SwitchCamera : Intent()
    }

    data class State(
        val isDisconnecting: Boolean = false,
        val localStream: UserMedia? = null,
        val remoteStream: UserMedia? = null,
    )

    sealed class Label {
        object Disconnected : Label()
    }
}