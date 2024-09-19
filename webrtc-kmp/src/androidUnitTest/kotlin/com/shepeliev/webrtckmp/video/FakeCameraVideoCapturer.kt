package com.shepeliev.webrtckmp.video

import android.content.Context
import org.webrtc.CameraVideoCapturer
import org.webrtc.CapturerObserver
import org.webrtc.SurfaceTextureHelper

class FakeCameraVideoCapturer(private var isFrontFacing: Boolean) : CameraVideoCapturer {
    override fun initialize(
        textureHelper: SurfaceTextureHelper,
        context: Context,
        observer: CapturerObserver
    ) {
        // no-op
    }

    override fun startCapture(width: Int, height: Int, fps: Int) {
        // no-op
    }

    override fun stopCapture() {
        // no-op
    }

    override fun changeCaptureFormat(width: Int, height: Int, fps: Int) {
        // no-op
    }

    override fun dispose() {
        // no-op
    }

    override fun isScreencast(): Boolean = false

    override fun switchCamera(handler: CameraVideoCapturer.CameraSwitchHandler) {
        isFrontFacing = !isFrontFacing
        handler.onCameraSwitchDone(isFrontFacing)
    }

    override fun switchCamera(handler: CameraVideoCapturer.CameraSwitchHandler, deviceId: String) {
        val name = TwoCamerasEnumerator.deviceNames.find { it == deviceId }
        if (name != null) {
            isFrontFacing = TwoCamerasEnumerator.isFrontFacing(name)
            handler.onCameraSwitchDone(isFrontFacing)
        } else {
            handler.onCameraSwitchError("Device not found: $deviceId")
        }
    }
}
