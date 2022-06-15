import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.shepeliev.webrtckmp.sample.shared.RoomComponent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import kotlinx.browser.document
import react.Fragment
import react.create
import react.dom.client.createRoot

fun main() {
    val firebaseOptions = FirebaseOptions(
        applicationId = "1:216132728347:web:f10a385863ec2d43872abe",
        apiKey = "AIzaSyDa0FDyeGNZZcKKBXnALeJSqfUxSNKut4w",
        authDomain = "app-rtc-kmp.firebaseapp.com",
        projectId = "app-rtc-kmp",
        storageBucket = "app-rtc-kmp.appspot.com",
        gcmSenderId = "216132728347",
    )
    Firebase.initialize(null, firebaseOptions)

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
