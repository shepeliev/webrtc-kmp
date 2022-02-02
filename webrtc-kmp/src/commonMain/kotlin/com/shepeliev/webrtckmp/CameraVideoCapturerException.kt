package com.shepeliev.webrtckmp

class CameraVideoCapturerException(message: String) : Exception(message) {
    companion object {
        fun notFound(constraints: AudioTrackConstraints): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera not found. $constraints")
        }

        fun notFound(constraints: VideoTrackConstraints): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera not found. $constraints")
        }

        fun notFound(facingMode: FacingMode): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera not found: $facingMode")
        }

        fun notFound(cameraId: String): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera ID: $cameraId not found")
        }

        fun capturerStopped(): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera video capturer stopped")
        }
    }
}
