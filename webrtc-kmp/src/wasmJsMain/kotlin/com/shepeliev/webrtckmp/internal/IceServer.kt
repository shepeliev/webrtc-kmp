package com.shepeliev.webrtckmp.internal

import com.shepeliev.webrtckmp.IceServer

internal external interface JsIceServer : JsAny

internal fun IceServer.toWasmJs(): JsIceServer = createIceServer(urls.toJsArray(), username, password)

@Suppress("UNUSED_PARAMETER")
private fun createIceServer(urls: JsArray<JsString>, username: String?, credential: String?): JsIceServer = js(
    """
    ({
        urls: urls,
        username: username,
        credential: credential
    })
    """
)
