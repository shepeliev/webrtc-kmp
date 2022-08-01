import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.shepeliev.webrtckmp.sample.shared.RoomComponent
import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.client.createRoot

fun main() {
    val container = document.getElementById("root") ?: error("No root element.")
    val root = createRoot(container)

    root.render(Fragment.create {
        App {
            val lifecycle = LifecycleRegistry().apply { attachToDocument() }
            val context = DefaultComponentContext(lifecycle)
            room = RoomComponent(context)
        }
    })
}
