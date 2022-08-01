import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.ValueObserver
import com.shepeliev.webrtckmp.sample.shared.Room
import csstype.px
import emotion.react.css
import kotlinx.browser.window
import mui.material.Button
import mui.material.ButtonVariant
import org.w3c.dom.HTMLVideoElement
import react.*
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.p
import react.dom.html.ReactHTML.video

external interface AppProps : Props {
    var room: Room
}

val App = FC<AppProps> { props ->
    h1 { +"WebRTC KMP Sample" }

    val room = props.room
    val roomModel: Room.Model = useValue(room.model)

    roomModel.roomId?.let {
        p { +"Room ID: ${roomModel.roomId}" }
    }

    div {
        Button {
            variant = ButtonVariant.contained
            onClick = { room.openUserMedia() }
            disabled = roomModel.localStream != null
            +"Open camera and microphone"
        }

        Button {
            variant = ButtonVariant.contained
            onClick = { room.createRoom() }
            disabled = roomModel.isJoining || roomModel.localStream == null || roomModel.roomId != null
            +"Create room"
        }

        Button {
            variant = ButtonVariant.contained
            onClick = { window.alert("Yohohoh") }
            +"Join room"
        }

        Button {
            variant = ButtonVariant.contained
            onClick = { room.hangup() }
            +"Hangup"
        }
    }

    val localVideoRef = useRef<HTMLVideoElement>(null)
    useEffect(roomModel, localVideoRef) {
        localVideoRef.current?.srcObject = roomModel.localStream?.js
    }

    val remoteVideoRef = useRef<HTMLVideoElement>(null)
    useEffect {
        remoteVideoRef.current?.srcObject = roomModel.remoteStream?.js
    }

    div {
        video {
            css {
                width = 640.px
                height = 480.px
            }
            ref = localVideoRef
            autoPlay = true
        }

        if (roomModel.remoteStream != null) {
            video {
                css {
                    width = 640.px
                    height = 480.px
                }
                ref = remoteVideoRef
                autoPlay = true
            }
        }
    }
}

fun <T : Any> useValue(value: Value<T>): T {
    var result by useState(value.value)

    useEffect(value, result) {
        val valueObserver: ValueObserver<T> = { result = it }
        value.subscribe(valueObserver)
        cleanup { value.unsubscribe(valueObserver) }
    }

    return result
}
