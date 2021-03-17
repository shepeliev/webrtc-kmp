package com.shepeliev.apprtckmm.shared.call

import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.events
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.shepeliev.apprtckmm.shared.call.CallStoreFactory.Action.JoinRoom
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal val stateToModel: suspend CallStore.State.() -> CallView.Model = {
    CallView.Model(localStream, remoteStream)
}

internal val eventToIntent: suspend CallView.Event.() -> CallStore.Intent = {
    when (this) {
        CallView.Event.HangupClicked -> CallStore.Intent.Disconnect
        CallView.Event.SwitchCameraClicked -> CallStore.Intent.SwitchCamera
    }
}

class CallController(roomUrl: String, roomId: String, private val lifecycle: Lifecycle) {

    private val store = CallStoreFactory(DefaultStoreFactory).create(JoinRoom(roomUrl, roomId))

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onViewCreated(view: CallView, viewLifecycle: Lifecycle) {
        bind(viewLifecycle, BinderLifecycleMode.START_STOP) {
            store.states.map(stateToModel) bindTo view
            view.events.map(eventToIntent) bindTo store
            store.labels.filter { it is CallStore.Label.Disconnected } bindTo { view.navigateBack() }
        }
    }
}