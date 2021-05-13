package com.shepeliev.webrtckmp

expect fun runTest(block: suspend () -> Unit)

expect fun initializeWebRtc()

expect fun disposeWebRtc()
