package com.shepeliev.webrtckmm

import android.content.Context
import android.util.AttributeSet
import com.shepeliev.webrtckmm.android.EglBaseProvider
import org.webrtc.SurfaceViewRenderer

actual class RtcVideoView(
    context: Context,
    attrs: AttributeSet,
) : SurfaceViewRenderer(context, attrs) {

    actual var userMedia: UserMedia? = null
        set(value) {
            if (value == null) {
                release()
            } else if (field == null) {
                init(EglBaseProvider.getEglBase().eglBaseContext, null)
            }

            field?.also { it.videoTracks.forEach { track -> track.native.removeSink(this@RtcVideoView) } }
            value?.also { it.videoTracks.firstOrNull()?.native?.addSink(this@RtcVideoView) }
            field = value
        }
}