import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.client.createRoot
import web.dom.Element

fun main() {
    val container = document.getElementById("root") ?: error("No root element.")
    val root = createRoot(container as Element)

    root.render(
        Fragment.create {
            ReactApp {
            }
        }
    )
}
