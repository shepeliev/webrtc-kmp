import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import react.useEffect

fun useCoroutineScope(): CoroutineScope {
    val scope = MainScope()
    useEffect(Unit) {
        try {
            awaitCancellation()
        } finally {
            scope.cancel()
        }
    }
    return scope
}
