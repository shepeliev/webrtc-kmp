package com.shepeliev.webrtckmm.sample.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

actual fun CommonMainScope(): CoroutineScope = MainScope()