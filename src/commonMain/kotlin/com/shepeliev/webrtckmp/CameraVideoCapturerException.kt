package com.shepeliev.webrtckmp

class CameraVideoCapturerException(message: String) : Exception(message) {
    companion object {
        fun notFound(constraints: VideoConstraints): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera not found. $constraints")
        }

        fun capturerStopped(): CameraVideoCapturerException {
            return CameraVideoCapturerException("Camera video capturer stopped")
        }
    }
}
