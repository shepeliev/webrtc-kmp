package com.shepeliev.webrtckmp

public data class MediaTrackConstraints(
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
    val highpassFilter: ValueOrConstrain<Boolean>? = null,
    val typingNoiseDetection: ValueOrConstrain<Boolean>? = null,
)

public enum class FacingMode { User, Environment }

public sealed interface ValueOrConstrain<T> {
    public data class Value<T>(
        val value: T,
    ) : ValueOrConstrain<T>

    public data class Constrain<T>(
        var exact: T? = null,
        var ideal: T? = null,
    ) : ValueOrConstrain<T> {
        public fun exact(value: T) {
            exact = value
        }

        public fun ideal(value: T) {
            ideal = value
        }
    }
}

public fun Boolean.asValueConstrain() = ValueOrConstrain.Value(this)

public fun Int.asValueConstrain() = ValueOrConstrain.Value(this)

public fun Double.asValueConstrain() = ValueOrConstrain.Value(this)

public fun FacingMode.asValueConstrain() = ValueOrConstrain.Value(this)

public val <T> ValueOrConstrain<T>.value: T?
    get() =
        when (this) {
            is ValueOrConstrain.Value -> value
            is ValueOrConstrain.Constrain -> exact ?: ideal
        }

public val <T> ValueOrConstrain<T>.exact: T?
    get() =
        when (this) {
            is ValueOrConstrain.Value -> value
            is ValueOrConstrain.Constrain -> exact
        }

public val <T> ValueOrConstrain<T>.ideal: T?
    get() =
        when (this) {
            is ValueOrConstrain.Value -> value
            is ValueOrConstrain.Constrain -> ideal
        }

public fun <T, R> ValueOrConstrain<T>.map(transform: (T) -> R): ValueOrConstrain<R> =
    when (this) {
        is ValueOrConstrain.Value -> ValueOrConstrain.Value(transform(value))
        is ValueOrConstrain.Constrain ->
            ValueOrConstrain.Constrain(
                exact?.let(transform),
                ideal?.let(transform),
            )
    }

public class MediaTrackConstraintsBuilder(
    internal var constraints: MediaTrackConstraints,
) {
    public fun deviceId(id: String) {
        constraints = constraints.copy(deviceId = id)
    }

    public fun groupId(id: String) {
        constraints = constraints.copy(groupId = id)
    }

    public fun autoGainControl(enable: Boolean = true) {
        constraints = constraints.copy(autoGainControl = enable.asValueConstrain())
    }

    public fun autoGainControl(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(autoGainControl = constrain)
    }

    public fun channelCount(count: Int) {
        constraints = constraints.copy(channelCount = count.asValueConstrain())
    }

    public fun channelCount(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(channelCount = constrain)
    }

    public fun echoCancellation(enable: Boolean = true) {
        constraints = constraints.copy(echoCancellation = enable.asValueConstrain())
    }

    public fun echoCancellation(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(echoCancellation = constrain)
    }

    public fun highpassFilter(enable: Boolean = true) {
        constraints = constraints.copy(highpassFilter = enable.asValueConstrain())
    }

    public fun highpassFilter(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(highpassFilter = constrain)
    }

    public fun latency(latency: Double) {
        constraints = constraints.copy(latency = latency.asValueConstrain())
    }

    public fun latency(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(latency = constrain)
    }

    public fun noiseSuppression(enable: Boolean = true) {
        constraints = constraints.copy(noiseSuppression = enable.asValueConstrain())
    }

    public fun noiseSuppression(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(noiseSuppression = constrain)
    }

    public fun sampleRate(count: Int) {
        constraints = constraints.copy(sampleRate = count.asValueConstrain())
    }

    public fun sampleRate(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(sampleRate = constrain)
    }

    public fun sampleSize(count: Int) {
        constraints = constraints.copy(sampleSize = count.asValueConstrain())
    }

    public fun sampleSize(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(sampleRate = constrain)
    }

    public fun typingNoiseDetection(enable: Boolean = true) {
        constraints = constraints.copy(typingNoiseDetection = enable.asValueConstrain())
    }

    public fun typingNoiseDetection(build: ValueOrConstrain.Constrain<Boolean>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Boolean>()
        build(constrain)
        constraints = constraints.copy(typingNoiseDetection = constrain)
    }

    public fun aspectRatio(ratio: Double) {
        constraints = constraints.copy(aspectRatio = ratio.asValueConstrain())
    }

    public fun aspectRatio(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(aspectRatio = constrain)
    }

    public fun facingMode(mode: FacingMode) {
        constraints = constraints.copy(facingMode = mode.asValueConstrain())
    }

    public fun facingMode(build: ValueOrConstrain.Constrain<FacingMode>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<FacingMode>()
        build(constrain)
        constraints = constraints.copy(facingMode = constrain)
    }

    public fun frameRate(fps: Double) {
        constraints = constraints.copy(frameRate = fps.asValueConstrain())
    }

    public fun frameRate(build: ValueOrConstrain.Constrain<Double>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Double>()
        build(constrain)
        constraints = constraints.copy(frameRate = constrain)
    }

    public fun height(height: Int) {
        constraints = constraints.copy(height = height.asValueConstrain())
    }

    public fun height(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(height = constrain)
    }

    public fun width(width: Int) {
        constraints = constraints.copy(width = width.asValueConstrain())
    }

    public fun width(build: ValueOrConstrain.Constrain<Int>.() -> Unit) {
        val constrain = ValueOrConstrain.Constrain<Int>()
        build(constrain)
        constraints = constraints.copy(width = constrain)
    }
}
