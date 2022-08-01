package com.shepeliev.webrtckmp.sample.shared

data class QuerySnapshotObserver(
    @JsName("next")
    val next: (QuerySnapshot) -> Unit,
    @JsName("error")
    val error: (Throwable) -> Unit,
    @JsName("complete")
    val complete: () -> Unit,
)
