import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.stop
import kotlinx.browser.document
import org.w3c.dom.Document

fun LifecycleRegistry.attachToDocument() {
    fun onVisibilityChanged() {
        when (document.visibilityState) {
            "visible" -> resume()
            else -> stop()
        }
    }

    document.addEventListener("visibilitychange", callback = { onVisibilityChanged() })
}

private val Document.visibilityState: String get() = asDynamic().visibilityState.unsafeCast<String>()
