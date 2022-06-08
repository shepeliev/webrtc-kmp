package com.shepeliev.webrtckmp.sample

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.shepeliev.webrtckmp.VideoStreamTrack
import com.shepeliev.webrtckmp.eglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoSink

@Composable
fun Video(
    track: VideoStreamTrack,
    modifier: Modifier = Modifier,
    scalingTypeMatchOrientation: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_BALANCED,
    scalingTypeMismatchOrientation: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_FIT,
) {
    var renderer by remember { mutableStateOf<SurfaceViewRenderer?>(null) }

    val lifecycleEventObserver = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    renderer?.also {
                        it.init(eglBase.eglBaseContext, null)
                        track.addSinkCatching(it)
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    renderer?.also { track.removeSinkCatching(it) }
                    renderer?.release()
                }

                else -> {
                    // ignore other events
                }
            }
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, lifecycleEventObserver) {
        lifecycle.addObserver(lifecycleEventObserver)

        onDispose {
            renderer?.let { track.removeSinkCatching(it) }
            renderer?.release()
            lifecycle.removeObserver(lifecycleEventObserver)
        }
    }

    DisposableEffect(track) {
        renderer?.let { track.addSinkCatching(it) }

        onDispose { renderer?.let { track.removeSinkCatching(it) } }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceViewRenderer(context).apply {
                setScalingType(scalingTypeMatchOrientation, scalingTypeMismatchOrientation)
                renderer = this
            }
        }
    )
}

private fun VideoStreamTrack.addSinkCatching(sink: VideoSink) {
    // runCatching as track may be disposed while activity was in pause mode
    runCatching { addSink(sink) }
}

private fun VideoStreamTrack.removeSinkCatching(sink: VideoSink) {
    // runCatching as track may be disposed while activity was in pause mode
    runCatching { removeSink(sink) }
}
