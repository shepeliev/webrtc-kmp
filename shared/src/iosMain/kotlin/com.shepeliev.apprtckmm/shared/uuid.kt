package com.shepeliev.apprtckmm.shared

import platform.Foundation.NSUUID

actual fun uuid(): String = NSUUID().UUIDString
