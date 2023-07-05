package com.shepeliev.webrtckmp

data class MediaStreamConstraints(
    val audio: MediaTrackConstraints? = null,
    val video: MediaTrackConstraints? = null,
)

class MediaStreamConstraintsBuilder {
    internal var constraints = MediaStreamConstraints()

    fun audio(enabled: Boolean = true) {
        if (enabled) {
            audio { }
        }
    }

    fun audio(build: MediaTrackConstraintsBuilder.() -> Unit) {
        val trackConstraintsBuilder = MediaTrackConstraintsBuilder(
            constraints.audio ?: MediaTrackConstraints()
        )
        build(trackConstraintsBuilder)
        constraints = constraints.copy(audio = trackConstraintsBuilder.constraints)
    }

    fun video(enabled: Boolean = true) {
        if (enabled) {
            video { }
        }
    }

    fun video(build: MediaTrackConstraintsBuilder.() -> Unit) {
        val trackConstraintsBuilder = MediaTrackConstraintsBuilder(
            constraints.video ?: MediaTrackConstraints()
        )
        build(trackConstraintsBuilder)
        constraints = constraints.copy(video = trackConstraintsBuilder.constraints)
    }
}
