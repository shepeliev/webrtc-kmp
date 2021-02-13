package com.shepeliev.apprtckmm.shared

import kotlinx.coroutines.runBlocking

actual fun runTest(test: suspend () -> Unit) = runBlocking { test() }
