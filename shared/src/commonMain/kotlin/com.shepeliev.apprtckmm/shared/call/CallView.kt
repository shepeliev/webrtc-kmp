package com.shepeliev.apprtckmm.shared.call

import com.arkivanov.mvikotlin.core.view.MviView
import com.shepeliev.apprtckmm.shared.call.CallView.Event
import com.shepeliev.apprtckmm.shared.call.CallView.Model
import com.shepeliev.webrtckmm.VideoStream

interface CallView : MviView<Model, Event> {

    data class Model(val localVideo: VideoStream?, val remoteVideo: VideoStream?)

    sealed class Event {
        object HangupClicked : Event()
        object SwitchCameraClicked : Event()
    }

    fun navigateBack()
}