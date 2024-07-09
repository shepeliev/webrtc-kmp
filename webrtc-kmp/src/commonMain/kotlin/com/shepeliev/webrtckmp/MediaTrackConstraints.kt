package com.shepeliev.webrtckmp

data class MediaTrackConstraints(
    val aspectRatio: ValueOrConstrain<Double>? = null,
    val autoGainControl: ValueOrConstrain<Boolean>? = null,
    val channelCount: ValueOrConstrain<Int>? = null,
    val deviceId: String? = null,
    val echoCancellation: ValueOrConstrain<Boolean>? = null,
    val facingMode: ValueOrConstrain<FacingMode>? = null,
    val frameRate: ValueOrConstrain<Double>? = null,
    val groupId: String? = null,
    val height: ValueOrConstrain<Int>? = null,
    val latency: ValueOrConstrain<Double>? = null,
    val noiseSuppression: ValueOrConstrain<Boolean>? = null,
    val sampleRate: ValueOrConstrain<Int>? = null,
    val sampleSize: ValueOrConstrain<Int>? = null,
    val width: ValueOrConstrain<Int>? = null,
)

enum class FacingMode { User, Environment }

sealed interface ValueOrConstrain<T> {
    data class Value<T>(val value: T) : ValueOrConstrain<T>
    data class Constrain<T>(var exact: T? = null, var ideal: T? = null) : ValueOrConstrain<T> {
        fun exact(value: T) {
            exact = value
        }

        fun ideal(value: T) {
            ideal = value
        }
    }
}

fun Boolean.asValueConstrain() = ValueOrConstrain.Value(this)
fun Int.asValueConstrain() = ValueOrConstrain.Value(this)
fun Double.asValueConstrain() = ValueOrConstrain.Value(this)
fun FacingMode.asValueConstrain() = ValueOrConstrain.Value(this)

val <T> ValueOrConstrain<T>.value: T?
    get() = when (this) {
        is ValueOrConstrain.Value -> value
        is ValueOrConstrain.Constrain -> exact ?: ideal
    }

val <T> ValueOrConstrain<T>.exact: T?
    get() = when (this) {
        is ValueOrConstrain.Value -> value
        is ValueOrConstrain.Constrain -> exact
    }

val <T> ValueOrConstrain<T>.ideal: T?
    get() = when (this) {
        is ValueOrConstrain.Value -> value
        is ValueOrConstrain.Constrain -> ideal
    }

fun <T, R> ValueOrConstrain<T>.map(transform: (T) -> R): ValueOrConstrain<R> = when (this) {
    is ValueOrConstrain.Value -> ValueOrConstrain.Value(transform(value))
    is ValueOrConstrain.Constrain -> ValueOrConstrain.Constrain(
        exact?.let(transform),
        ideal?.let(transform)
    )
}

class MediaTrackConstraintsBuilder(internal var constraints: MediaTrackConstraints) {
    fun deviceId(id: String) {
        constraints = constraints.copy(deviceId = id)
    }

    fun groupId(id: String) {
        constraints = constraints.copy(groupId = id)
    }

    fun autoGainControl(enable: Boolean = true) {
        constraints = constraints.copy(autoGainControl = enable.asValueConstrain())
    }

    fun autoGainControl(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(autoGainControl = constrain)
    }

    fun channelCount(count: Int) {
        constraints = constraints.copy(channelCount = count.asValueConstrain())
    }

    fun channelCount(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(channelCount = constrain)
    }

    fun echoCancellation(enable: Boolean = true) {
        constraints = constraints.copy(echoCancellation = enable.asValueConstrain())
    }

    fun echoCancellation(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(echoCancellation = constrain)
    }

    fun latency(latency: Double) {
        constraints = constraints.copy(latency = latency.asValueConstrain())
    }

    fun latency(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(latency = constrain)
    }

    fun noiseSuppression(enable: Boolean = true) {
        constraints = constraints.copy(noiseSuppression = enable.asValueConstrain())
    }

    fun noiseSuppression(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(noiseSuppression = constrain)
    }

    fun sampleRate(count: Int) {
        constraints = constraints.copy(sampleRate = count.asValueConstrain())
    }

    fun sampleRate(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(sampleRate = constrain)
    }

    fun sampleSize(count: Int) {
        constraints = constraints.copy(sampleSize = count.asValueConstrain())
    }

    fun sampleSize(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(sampleRate = constrain)
    }

    fun aspectRatio(ratio: Double) {
        constraints = constraints.copy(aspectRatio = ratio.asValueConstrain())
    }

    fun aspectRatio(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(aspectRatio = constrain)
    }

    fun facingMode(mode: FacingMode) {
        constraints = constraints.copy(facingMode = mode.asValueConstrain())
    }

    fun facingMode(build: ValueOrConstrain.Constrain<FacingMode>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<FacingMode>()
        build(constrain)
        constraints = constraints.copy(facingMode = constrain)
    }

    fun frameRate(fps: Double) {
        constraints = constraints.copy(frameRate = fps.asValueConstrain())
    }

    fun frameRate(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(frameRate = constrain)
    }

    fun height(height: Int) {
        constraints = constraints.copy(height = height.asValueConstrain())
    }

    fun height(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(height = constrain)
    }

    fun width(width: Int) {
        constraints = constraints.copy(width = width.asValueConstrain())
    }

    fun width(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(width = constrain)
    }
}
