package com.shepeliev.webrtckmm.utils

import platform.Foundation.NSUUID

actual fun uuid(): String =  NSUUID.UUID().UUIDString()