import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import react.useEffect

fun useCoroutineScope(): CoroutineScope {
    val scope = MainScope()
    useEffect(Unit) {
        cleanup { scope.cancel() }
    }
    return scope
}
