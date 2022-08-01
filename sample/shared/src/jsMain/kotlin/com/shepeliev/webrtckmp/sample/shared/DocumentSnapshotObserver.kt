package com.shepeliev.webrtckmp.sample.shared

data class DocumentSnapshotObserver(
    @JsName("next")
    val next: (DocumentSnapshot) -> Unit,
    @JsName("error")
    val error: (Throwable) -> Unit,
    @JsName("complete")
    val complete: () -> Unit,
)
