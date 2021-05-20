package com.shepeliev.webrtckmp.utils

import platform.Foundation.NSUUID

actual fun uuid(): String = NSUUID.UUID().UUIDString()
