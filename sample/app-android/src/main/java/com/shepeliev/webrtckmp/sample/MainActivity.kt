package com.shepeliev.webrtckmp.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.shepeliev.webrtckmp.initializeWebRtc
import com.shepeliev.webrtckmp.sample.shared.RoomComponent

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeWebRtc(this)

        val room = RoomComponent(componentContext = defaultComponentContext())
        setContent { App(room) }
    }
}
