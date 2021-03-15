package com.shepeliev.apprtckmm.shared.call

import com.arkivanov.mvikotlin.core.view.MviView
import com.shepeliev.apprtckmm.shared.call.CallView.Event
import com.shepeliev.apprtckmm.shared.call.CallView.Model
import com.shepeliev.webrtckmm.VideoTrack

interface CallView : MviView<Model, Event> {

    data class Model(
        val localVideoTrack: VideoTrack?,
        val remoteVideoTrack: VideoTrack?,
    )

    sealed class Event {
        object HangupClicked : Event()
        object SwitchCameraClicked : Event()
    }

    fun navigateBack()
}