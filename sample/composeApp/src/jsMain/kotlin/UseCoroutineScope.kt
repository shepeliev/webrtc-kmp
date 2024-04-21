import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import react.useEffect

fun useCoroutineScope(): CoroutineScope {
    val scope = CoroutineScope(Dispatchers.Main)
    useEffect(Unit) {
        cleanup { scope.cancel() }
    }
    return scope
}
