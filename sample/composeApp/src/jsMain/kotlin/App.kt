import com.shepeliev.webrtckmp.MediaDevices
import com.shepeliev.webrtckmp.MediaStream
import com.shepeliev.webrtckmp.PeerConnection
import com.shepeliev.webrtckmp.videoTracks
import emotion.react.css
import kotlinx.coroutines.launch
import mui.material.Button
import mui.material.ButtonVariant
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.video
import react.useEffect
import react.useRef
import react.useState
import web.cssom.Display
import web.cssom.JustifyContent
import web.cssom.px
import web.html.HTMLVideoElement

val ReactApp = FC<Props> { _ ->
    h1 { +"WebRTC KMP Sample" }

    val scope = useCoroutineScope()
    val localVideoRef = useRef<HTMLVideoElement>(null)
    val remoteVideoRef = useRef<HTMLVideoElement>(null)
    val remoteStream = useRef<MediaStream>(null)
    val (localStream, setLocalStream) = useState<MediaStream?>(null)
    val (peerConnections, setPeerConnections) = useState<Pair<PeerConnection, PeerConnection>?>(null)

    useEffect(localStream) {
        val localVideoStream = MediaStream().apply { localStream?.videoTracks?.firstOrNull()?.let { addTrack(it) } }
        localVideoRef.current?.srcObject = localVideoStream.js
    }

    useEffect(localStream, peerConnections) {
        if (peerConnections == null || localStream == null) return@useEffect
        remoteStream.current = MediaStream()
        val job = scope.launch {
            makeCall(
                peerConnections = peerConnections,
                localStream = localStream,
                onRemoteVideoTrack = { track ->
                    remoteStream.current?.addTrack(track)
                    remoteVideoRef.current?.srcObject = remoteStream.current?.js
                },
                onRemoteAudioTrack = { track ->
                    remoteStream.current?.addTrack(track)
                    remoteVideoRef.current?.srcObject = remoteStream.current?.js
                },
            )
        }

        cleanup { job.cancel() }
    }

    div {
        video {
            css {
                width = 640.px
                height = 480.px
                paddingRight = 32.px
            }
            ref = localVideoRef
            autoPlay = true
        }

        video {
            css {
                width = 640.px
                height = 480.px
            }
            ref = remoteVideoRef
            autoPlay = true
        }
    }

    div {
        css {
            width = 640.px
            display = Display.flex
            justifyContent = JustifyContent.spaceBetween
        }

        if (localStream == null) {
            Button {
                css {
                    width = 200.px
                }
                variant = ButtonVariant.contained
                onClick = {
                    scope.launch {
                        val stream = MediaDevices.getUserMedia(audio = true, video = true)
                        setLocalStream(stream)
                    }
                }
                +"Start"
            }

            return@div
        }

        Button {
            css {
                width = 200.px
            }
            variant = ButtonVariant.contained
            onClick = {
                hangup(peerConnections)
                localStream.release()
                setLocalStream(null)
                setPeerConnections(null)
                remoteVideoRef.current?.srcObject = null
            }
            +"Stop"
        }

        if (peerConnections == null) {
            Button {
                css {
                    width = 200.px
                }
                variant = ButtonVariant.contained
                onClick = {
                    setPeerConnections(Pair(PeerConnection(), PeerConnection()))
                }
                +"Call"
            }
        } else {
            Button {
                css {
                    width = 200.px
                }
                variant = ButtonVariant.contained
                onClick = {
                    hangup(peerConnections)
                    setPeerConnections(null)
                    remoteVideoRef.current?.srcObject = null
                }
                +"Hangup"
            }
        }
    }
}
