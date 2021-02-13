package com.shepeliev.apprtckmm

import platform.Foundation.NSUUID

actual fun uuid(): String = NSUUID().UUIDString
