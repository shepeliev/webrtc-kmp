package com.shepeliev.webrtckmp.utils

import java.util.UUID

actual fun uuid(): String = UUID.randomUUID().toString()
