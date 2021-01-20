package com.shepeliev.webrtckmm

expect class MediaConstraints(
    mandatory: Map<String, String> = emptyMap(),
    optional: Map<String, String> = emptyMap()
)

class MediaConstraintsBuilder internal constructor() {
    private val mandatory: MutableMap<String, String> = mutableMapOf()
    private val optional: MutableMap<String, String> = mutableMapOf()

    fun mandatory(add: () -> Pair<String, String>) {
        mandatory += add()
    }

    fun optional(add: () -> Pair<String, String>) {
        optional += add()
    }

    internal fun build(): MediaConstraints = MediaConstraints(mandatory, optional)
}

fun mediaConstraints(build: MediaConstraintsBuilder.() -> Unit): MediaConstraints {
    val builder = MediaConstraintsBuilder()
    build(builder)
    return builder.build()
}
